# ESP32 Mobile-Controlled Laser Turret

An ESP32-powered, two-axis (pan and tilt) laser turret remotely operated via a custom Android application. Originally developed to merge hardware sensor integration with mobile app development, this project translates real-time smartphone telemetry—such as accelerometer data, touch inputs, and microphone levels—into physical turret movements.

## Features
* **2-DoF Pan & Tilt:** Uses a dual-servo mechanism integrated into a custom 3D-printed legged base for full yaw and pitch control.
* **Android Control App:** A custom-built mobile interface that communicates directly with the turret over Bluetooth or WiFi.
* **Sensor-Driven Targeting:** Maps onboard phone sensors (IMU/accelerometer, touch, and audio) to direct the laser module's orientation.

## Hardware Stack
* ESP32 Microcontroller
* 2x Hobby Servos (Pan & Tilt configuration)
* Laser Diode module
* Custom 3D-printed stand and servo mounts
* 5V Power Supply 

## Software Stack
* **Firmware:** C/C++ for the ESP32 (PlatformIO / Arduino IDE)
* **Mobile App:** Android Studio for Bluetooth/WiFi communication and sensor data extraction.
