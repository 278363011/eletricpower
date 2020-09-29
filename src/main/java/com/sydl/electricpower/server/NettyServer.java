package com.sydl.electricpower.server;

import com.sydl.electricpower.endecode.CustomMessageDecoder;
import com.sydl.electricpower.handler.ServerMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyServer {

    //核心配置类
    private final ServerBootstrap serverBootstrap;
    //Boss线程组
    private final EventLoopGroup bossEventLoopGroup;
    //Work线程组
    private final EventLoopGroup workEventLoopGroup;
    //是否启用多路复用io模型的epoll模式
    private boolean useEPool = false;
    //服务器绑定端口
    private int port =8899;

    public NettyServer() {
        this(false,8899);
    }

    public NettyServer(boolean useEPool,int port) {
        //初始化
        bossEventLoopGroup = new NioEventLoopGroup();
        workEventLoopGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        this.useEPool = useEPool;
        this.port=port;
    }


    private void start() {
            //核心配置
            serverBootstrap
                    .group(bossEventLoopGroup, workEventLoopGroup)
                    .channel(useEPool ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)                 // sync + accept = backlog
                    .option(ChannelOption.SO_REUSEADDR, true)               // 允许重复使用本地地址和端口
                    .option(ChannelOption.SO_KEEPALIVE, false)             // 如果在两小时内没有数据的通信时, TCP会自动发送一个活动探测数据报文
                    .handler(new LoggingHandler(LogLevel.INFO))                  // 设置服务器日志级别
                    .childOption(ChannelOption.TCP_NODELAY, true)          // 该参数的作用就是禁止使用Nagle算法，使用于小数据即时传输
                    .childOption(ChannelOption.SO_SNDBUF, 65535)           // 设置发送数据的缓冲区大小
                    .childOption(ChannelOption.SO_RCVBUF, 65535)           // 设置接受数据的缓冲区大小
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // 使用对象池，重用缓冲区
                    .localAddress(new InetSocketAddress(port))                   // 绑定端口
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 协议编解码
                            socketChannel.pipeline().addLast(new CustomMessageDecoder());
                            socketChannel.pipeline().addLast(new ServerMessageHandler());
                        }
                    });
            //正式连接
            try {

                ChannelFuture channelFuture = serverBootstrap.bind().sync();
                log.info(" <----------- Server StartUp On Port :" + port + " ---------------> ");
                channelFuture.channel().closeFuture().sync();

            } catch (Throwable e) {

                log.error("#Server occur errors when Server initd . the cause is :{}", e.getMessage());

            } finally {
                //优雅关闭
                log.info(" <----------- Server shutdownGracefully ---------------> ");
                bossEventLoopGroup.shutdownGracefully();
                workEventLoopGroup.shutdownGracefully();
            }
    }


    public static void main(String[] args) {
        NettyServer nettyServer=new NettyServer();
        nettyServer.start();
    }

}
