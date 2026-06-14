package javachat.server;

public class ServerMain {
    public static void main(String[] args) {
        int port = 7999;

        // 可选：从命令行参数读取端口
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("端口号格式错误，使用默认端口：" + port);
            }
        }

        ChatServer server = new ChatServer(port);

        // 添加关闭钩子，保证优雅退出
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("正在关闭服务器...");
            server.stop();
        }));

        server.start();
    }
}