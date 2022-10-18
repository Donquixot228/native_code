 import UIKit
 import Flutter

 @UIApplicationMain
 @objc class AppDelegate: FlutterAppDelegate {
 override func application(
 _ application: UIApplication,
 didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
 ) -> Bool {
 let controller : FlutterViewController = window?.rootViewController as! FlutterViewController

 let batteryChannel = FlutterMethodChannel(name: "battery",
     binaryMessenger: self.flutterEngine.binaryMessenger)


 GeneratedPluginRegistrant.register(with: self)
 return super.application(application, didFinishLaunchingWithOptions: launchOptions)
 }
 }
