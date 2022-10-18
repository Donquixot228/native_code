import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter Demo',
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key}) : super(key: key);

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  late StreamSubscription streamSubscription;
  static const batteryChanel = MethodChannel('battery');
  static const chargingChannel = EventChannel('battery_event');

  String batteryLevel = 'Waiting...';
  String chargingLevel = 'Streaming...';

  @override
  void initState() {
    super.initState();
    // onListenBattery();
    // onStreamBattery();
  }

  @override
  void dispose() {
   // streamSubscription.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Container(
        padding: const EdgeInsets.all(32),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                batteryLevel,
                style: const TextStyle(
                  color: Colors.blue,
                  fontSize: 24,
                ),
              ),
              // Text(
              //   chargingLevel,
              //   style: const TextStyle(
              //     color: Colors.blue,
              //     fontSize: 24,
              //   ),
              // ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: getBatteryLevel,
                child: const Text('Get Battery Level'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future getBatteryLevel() async {
    final arguments = {'name': 'John'};
    final String result =
        await batteryChanel.invokeMethod('getBatteryLevel', arguments);
    setState(
      () {
        batteryLevel = '$result%';
      },
    );
  }

  void onListenBattery() async {
    batteryChanel.setMethodCallHandler(
      (call) async {
        if (call.method == 'onBatteryChanged') {
          setState(
            () {
              batteryLevel = '${call.arguments}%';
            },
          );
        }
      },
    );
  }

  void onStreamBattery() async {
    streamSubscription = chargingChannel.receiveBroadcastStream().listen(
      (event) {
        setState(
          () {
            chargingLevel = '$event';
          },
        );
      },
    );
  }
}
