package alemiz.stargate.gate;

import alemiz.stargate.StarGate;
import alemiz.stargate.gate.packets.StarGatePacket;
import net.md_5.bungee.api.ProxyServer;

import java.io.*;
import java.net.Socket;

class Handler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private String name;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public PrintWriter getOut() {
        return out;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void run() {
        try {
            //in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //out = new DataOutputStream(socket.getOutputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {

               name = in.readLine();
               if (name.startsWith("CHEVRON:") && name.length() > 8) {
                   name = name.substring(8);

                   StarGate.getInstance().getLogger().info("§aNew client connected: §6"+name);
                   StarGate.getInstance().getLogger().info("§aADDRESS: §e"+socket.getInetAddress().toString().replace("/", "")+":"+socket.getPort());

                   /*Sending message to Client to confirm successful Connection*/
                   out.println("GATE_OPENED");

                   GateAPI.getGateServer().clients.put(name, this);
                   break;
               }
            }

            GateAPI.ping(name);

            while (true) {
                String line = in.readLine();

                if (line.equals("GATE_CLOSE")) break;

                if (!GateAPI.getGateServer().processPacket(name, line)){
                    StarGate.getInstance().getLogger().info("§cWARNING: Unknown packet !");
                }
            }

            /*for (;;){
                if (line.equals("GATE_CLOSE")) break;
                line = in.readLine();

                if (!GateAPI.getGateServer().processPacket(line)){
                    StarGate.getInstance().getLogger().info("§cWARNING: Unknown packet !");
                }
            }*/

        } catch(IOException i) {
            if (i.getMessage() == "Connection reset" || i.getMessage() == "Stream closed") return;

            StarGate.getInstance().getLogger().info("§cERROR: Connection with §6"+name+"§c has been aborted!");
            System.out.println(i.getMessage());
        } finally {
            GateAPI.getGateServer().clients.remove(name);
            StarGate.getInstance().getLogger().info("§cWARNING: Connection with §6"+name+"§c has been closed!");

            try {
                in.close();
                out.close();
                socket.close();
            } catch(IOException e) {}
        }
    }

    public boolean reconnect(){
        StarGate.getInstance().getLogger().info("§cWARNING: Reconnecting §6"+name+"§c!");
        try {
            out.println("GATE_RECONNECT");

            in.close();
            out.close();
            socket.close();
        } catch(IOException e) {return  false; }
        return true;
    }

    public void gatePacket(StarGatePacket packet){
        String data;
        if (!packet.isEncoded) {
            packet.encode();
        }

        data = packet.encoded;

        try {
            out.println(data);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}