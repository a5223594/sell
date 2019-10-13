package com.my.sell.listener;

import com.my.sell.rebuild.RebuildCacheQueue;
import com.my.sell.rebuild.RebuildCacheThread;
import com.my.sell.spring.SpringContext;
import com.my.sell.thread.RequestProcessorThreadPool;
import com.my.sell.zk.ZooKeeperSession;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@Component
public class MyServletContextListener implements ServletContextListener {


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        SpringContext.setApplicationContext(applicationContext);
        System.out.println("------注册的监听器启动了-----");
        RequestProcessorThreadPool.init();
        ZooKeeperSession.init();
        RebuildCacheQueue.init();
        new Thread(new RebuildCacheThread()).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
