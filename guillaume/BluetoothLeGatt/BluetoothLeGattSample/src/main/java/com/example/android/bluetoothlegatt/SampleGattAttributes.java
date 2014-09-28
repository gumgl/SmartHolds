/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static UUID SERVICE_ACC = UUID.fromString("0000aa10-0000-1000-8000-00805f9b34fb");
    public static UUID ACC_ENABLE_UUID = UUID.fromString("0000aa12-0000-1000-8000-00805f9b34fb");
    public static UUID ACC_XYZ_DATA8_UUID = UUID.fromString("0000aa13-0000-1000-8000-00805f9b34fb");
    public static UUID ACC_XYZ_DATA14_UUID = UUID.fromString("0000aa14-0000-1000-8000-00805f9b34fb");
    public static UUID ACC_TAP_DATA_UUID = UUID.fromString("0000aa15-0000-1000-8000-00805f9b34fb");
    public static UUID ACC_TAP_THSZ_UUID = UUID.fromString("0000aa16-0000-1000-8000-00805f9b34fb");
    public static UUID ACC_TAP_THSX_UUID = UUID.fromString("0000aa17-0000-1000-8000-00805f9b34fb");
    public static UUID ACC_TAP_THSY_UUID = UUID.fromString("0000aa18-0000-1000-8000-00805f9b34fb");
    public static UUID SERVICE_TEST = UUID.fromString("0000aa60-0000-1000-8000-00805f9b34fb");
    public static UUID TST_COLOR_UUID = UUID.fromString("0000aa62-0000-1000-8000-00805f9b34fb");

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
