package mao.t3;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * Project name(项目名称)：Netty_黏包与半包现象解决
 * Package(包名): mao.t3
 * Class(类名): Client
 * Author(作者）: mao
 * Author QQ：1296193245
 * GitHub：https://github.com/maomao124/
 * Date(创建日期)： 2023/3/24
 * Time(创建时间)： 21:51
 * Version(版本): 1.0
 * Description(描述)： 固定分隔符方式解决黏包与半包问题，客户端
 */

@Slf4j
public class Client
{
    public static void main(String[] args)
    {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try
        {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception
                {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter()
                    {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception
                        {
                            send(ctx, "123");
                            send(ctx, "hello");
                            send(ctx, "123456789012345678901234567890123456789012345678901234567890123456789" +
                                    "01234567890123456789012345678901234567890123456789012345678901234567890");
                            //中间带了\n
                            send(ctx, "321\n654");
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();
            channelFuture.channel().closeFuture().sync();

        }
        catch (InterruptedException e)
        {
            log.error("client error", e);
        }
        finally
        {
            worker.shutdownGracefully();
        }
    }

    /**
     * 发送消息,以\n结束
     *
     * @param ctx ctx
     * @param msg 消息
     */
    private static void send(ChannelHandlerContext ctx, String msg)
    {
        msg = msg + "\n";
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuf buffer = ctx.alloc().buffer(bytes.length);
        buffer.writeBytes(bytes);
        ctx.writeAndFlush(buffer);
    }
}
