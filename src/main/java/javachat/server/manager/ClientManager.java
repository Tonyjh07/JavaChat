package javachat.server.manager;

import javachat.common.Message;
import javachat.server.ClientHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {
    private final ConcurrentHashMap<String, ClientHandler> clients;

    public ClientManager() {
        this.clients = new ConcurrentHashMap<>();
    }

    public void addClient(String name, ClientHandler handler) {
        clients.put(name, handler);
        System.out.println("用户 " + name + " 已上线，当前在线人数：" + getOnlineCount());
    }

    public void removeClient(String name) {
        clients.remove(name);
        System.out.println("用户 " + name + " 已下线，当前在线人数：" + getOnlineCount());
    }

    public ClientHandler getClient(String name) {
        return clients.get(name);
    }

    public Map<String, ClientHandler> getAllClients() {
        return java.util.Collections.unmodifiableMap(clients);
    }

    public Set<String> getAllNames() {
        return clients.keySet();
    }

    public boolean isNameExists(String name) {
        return clients.containsKey(name);
    }

    public int getOnlineCount() {
        return clients.size();
    }

    public void broadcast(Message msg, ClientHandler exclude) {
        for (ClientHandler handler : clients.values()) {
            if (handler != exclude) {
                handler.sendMessage(msg);
            }
        }
    }

    public void broadcastToAll(Message msg) {
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(msg);
        }
    }

    public boolean sendToClient(String name, Message msg) {
        ClientHandler handler = clients.get(name);
        if (handler != null) {
            handler.sendMessage(msg);
            return true;
        }
        return false;
    }
}