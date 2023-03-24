package mao.t1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * Project name(项目名称)：Netty_黏包与半包现象解决
 * Package(包名): mao.t1
 * Class(类名): Server
 * Author(作者）: mao
 * Author QQ：1296193245
 * GitHub：https://github.com/maomao124/
 * Date(创建日期)： 2023/3/24
 * Time(创建时间)： 21:00
 * Version(版本): 1.0
 * Description(描述)： 短链接方式解决黏包与半包问题，服务端
 */

@Slf4j
public class Server
{
    @SneakyThrows
    public static void main(String[] args)
    {
        NioEventLoopGroup boos = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        Channel channel = new ServerBootstrap()
                .group(boos, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>()
                {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception
                    {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter()
                        {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception
                            {
                                log.debug("连接建立：" + ctx.channel());
                                super.channelActive(ctx);
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
                            {
                                ByteBuf byteBuf = (ByteBuf) msg;
                                log.debug("读事件:" + byteBuf);
                                super.channelRead(ctx, msg);
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception
                            {
                                log.debug("连接关闭：" + ctx.channel());
                                super.channelInactive(ctx);
                            }
                        });
                    }
                })
                .bind(8080)
                .sync().channel();
        channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>()
        {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception
            {
                boos.shutdownGracefully();
                worker.shutdownGracefully();
            }
        });
    }

}
