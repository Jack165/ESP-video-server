# ESP-video-server
基于摄像头采集的MJPEG的接受分发服务
JDK版本使用JDK21
Spring boot版本使用3.5

使用到依赖javacv 1.5.9

功能：
实时结算ESP32-CAM采集到数据，提供MJPEG流推送到分发服务，分发服务缓存十分钟内到视频流编码成H.265后通过WebSocket推送给网页客户端
