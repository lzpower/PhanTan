package service;

import network.ClientSession;
import network.RemoteServiceProxy;

import java.util.function.Supplier;

public final class ServiceFactory {

    private ServiceFactory() {
    }

    public static <T> T get(Class<T> serviceType, Supplier<T> localSupplier) {
        if (ClientSession.isRemoteMode()) {
            return RemoteServiceProxy.create(serviceType);
        }
        return localSupplier.get();
    }
}