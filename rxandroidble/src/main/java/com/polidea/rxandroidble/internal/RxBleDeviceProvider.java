package com.polidea.rxandroidble.internal;

import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.injection.ClientScope;
import com.polidea.rxandroidble.injection.DeviceComponent;
import com.polidea.rxandroidble.internal.cache.DeviceComponentCache;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

@ClientScope
public class RxBleDeviceProvider {

    private final Map<String, DeviceComponent> cachedDeviceComponents;
    private final Provider<DeviceComponent.Builder> deviceComponentBuilder;

    @Inject
    public RxBleDeviceProvider(DeviceComponentCache deviceComponentCache, Provider<DeviceComponent.Builder> deviceComponentBuilder) {
        this.cachedDeviceComponents = deviceComponentCache;
        this.deviceComponentBuilder = deviceComponentBuilder;
    }

    public RxBleDevice getBleDevice(String macAddress) {
        final DeviceComponent rxBleDevice = cachedDeviceComponents.get(macAddress);

        if (rxBleDevice != null) {
            return rxBleDevice.provideDevice();
        }

        synchronized (cachedDeviceComponents) {
            final DeviceComponent secondCheckRxBleDevice = cachedDeviceComponents.get(macAddress);

            if (secondCheckRxBleDevice != null) {
                return secondCheckRxBleDevice.provideDevice();
            }

            final DeviceComponent deviceComponent =
                    deviceComponentBuilder.get()
                    .deviceModule(new DeviceModule(macAddress))
                    .build();

            final RxBleDevice newRxBleDevice = deviceComponent.provideDevice();
            cachedDeviceComponents.put(macAddress, deviceComponent);
            return newRxBleDevice;
        }
    }
}
