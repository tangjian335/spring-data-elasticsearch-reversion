package org.springframework.data.elasticsearch.client;

import io.netty.util.ThreadDeathWatcher;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.percolator.PercolatorPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.mustache.MustachePlugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SpringDataTransportClient extends TransportClient {

    /**
     * Netty wants to do some unwelcome things like use unsafe and replace a private field, or use a poorly considered
     * buffer recycler. This method disables these things by default, but can be overridden by setting the corresponding
     * system properties.
     */
    private static void initializeNetty() {
        /*
         * We disable three pieces of Netty functionality here:
         *  - we disable Netty from being unsafe
         *  - we disable Netty from replacing the selector key set
         *  - we disable Netty from using the recycler
         *
         * While permissions are needed to read and set these, the permissions needed here are innocuous and thus should simply be granted
         * rather than us handling a security exception here.
         */
        setSystemPropertyIfUnset("io.netty.noUnsafe", Boolean.toString(true));
        setSystemPropertyIfUnset("io.netty.noKeySetOptimization", Boolean.toString(true));
        setSystemPropertyIfUnset("io.netty.recycler.maxCapacityPerThread", Integer.toString(0));
    }

    @SuppressForbidden(reason = "set system properties to configure Netty")
    private static void setSystemPropertyIfUnset(final String key, final String value) {
        final String currentValue = System.getProperty(key);
        if (currentValue == null) {
            System.setProperty(key, value);
        }
    }

    private static final List<String> OPTIONAL_DEPENDENCIES = Arrays.asList( //
            "org.elasticsearch.transport.Netty3Plugin", //
            "org.elasticsearch.transport.Netty4Plugin");

    private static final Collection<Class<? extends Plugin>> PRE_INSTALLED_PLUGINS;

    static {

        initializeNetty();

        List<Class<? extends Plugin>> plugins = new ArrayList<>();
        boolean found = false;

        for (String dependency : OPTIONAL_DEPENDENCIES) {
            try {
                plugins.add((Class<? extends Plugin>) ClassUtils.forName(dependency,
                        SpringDataTransportClient.class.getClassLoader()));
                found = true;
            } catch (ClassNotFoundException | LinkageError e) {
            }
        }

        Assert.state(found, "Neither Netty 3 or Netty 4 plugin found on the classpath. One of them is required to run the transport client!");

        plugins.add(ReindexPlugin.class);
        plugins.add(PercolatorPlugin.class);
        plugins.add(MustachePlugin.class);
//			plugins.add(ParentJoinPlugin.class);

        PRE_INSTALLED_PLUGINS = Collections.unmodifiableList(plugins);
    }

    public SpringDataTransportClient(Settings settings) {
        super(settings, PRE_INSTALLED_PLUGINS);
    }

    @Override
    public void close() {
        super.close();
        if (NetworkModule.TRANSPORT_TYPE_SETTING.exists(settings) == false
                || NetworkModule.TRANSPORT_TYPE_SETTING.get(settings).equals(Netty4Plugin.NETTY_TRANSPORT_NAME)) {
            try {
                GlobalEventExecutor.INSTANCE.awaitInactivity(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                ThreadDeathWatcher.awaitInactivity(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
		}
	}