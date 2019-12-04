package com.blockchain;

import java.io.*;
import java.util.*;
import java.net.*;

//The server
public class JavaServer
{

    // Vector to store active clients 
    static Vector<ClientHandler> clientVector = new Vector<>();
    static Admin admin;

    // counter for clients 
    static int i = 0;

    public static void main(String[] args) throws IOException {

        try {
            // server on port 1234, could be whatever port though
            ServerSocket server = new ServerSocket(1234);

            Socket sock;

            //starts up the admin for the server
            Scanner scanner = new Scanner(System.in);
            admin = new Admin(scanner);
            Thread adminThread = new Thread(admin);
            adminThread.start();

            System.out.println("The server is up.");

            // client request
            while (true) {
                // accepts the client connection
                sock = server.accept();

                System.out.println("New employee login : " + sock);

                // opens the data streams between the new client and server
                DataInputStream dis = new DataInputStream(sock.getInputStream());
                DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

                // Create a new handler for the client
                ClientHandler client = new ClientHandler(sock, "0" + i, dis, dos, admin);

                // puts the handler in a thread
                Thread thr = new Thread(client);

                // places client in vector
                clientVector.add(client);

                // starts the client thread, automatically calling run method from handler
                thr.start();

                i++;
            }

        }catch(Exception e) {

            System.out.println("Connection terminated.");
        }
    }
}

// Handler for a client connection 
class ClientHandler implements Runnable
{
    Scanner scn = new Scanner(System.in);
    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isloggedin;
    Employee emp;
    Admin admin;

    // constructor, also pretends to collect employee data from a database that doesn't exist
    public ClientHandler(Socket s, String name,
                         DataInputStream dis, DataOutputStream dos, Admin admin) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isloggedin=true;

        this.emp = new Employee(name, BlockChainKt.collectTokens());

        for(int d = 0; d < admin.blockChain.size(); d++) {

            //creates unique block lists for each employee
            this.emp.getEmployeeChain().add((Block) admin.blockChain.get(d).clone());
        }
        admin.empList.add(emp);
    }

    @Override
    public void run() {

        String received;

        try {

            System.out.println("com.blockchain.com.blockchain.Employee " + name + " has logged in.");
            dos.writeUTF("com.blockchain.com.blockchain.Employee ID: " + name + "\n" + "Token Balance: " + emp.getTokens() + "\n");
            dos.writeUTF("Input a number corresponding to the following commands: " + "\n");
            dos.writeUTF("[1]View your copy of the chain" + "\n" + "[2]Propose a block for forging" + "\n"
                    + "[3]Edit the most recent block to reflect your ownership..." + "\n"
                    + "[4]Try to buy an incentive." + "\n"
                    + "[5]Close the application" + "\n");

        }catch(IOException ioe) {
            System.out.println("Data output stream error.");
        }

        //this is the infinite loop that continually collects i/o from a client thread
        while (true)
        {
            try
            {
                // receive the command from client
                received = dis.readUTF();

                //command to close a client connection
                if(received.equals("5")){
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }

                //just shows an employee their copy of the chain
                if(received.equals("1")) {
                    dos.writeUTF(this.emp.prettyBlockChain());
                }

                //proposes a stake to the server. includes consensus verification that it is a valid block
                if(received.equals("2")) {

                    Block stake = this.emp.generateStakeBlock("Owner: " + name, name);

                    Boolean validity = false;
                    LinkedList<String> consensusList = new LinkedList<String>();

                    //loops through the other nodes and checks the staked block against their ledgers
                    for(int i = 0; i <= (JavaServer.admin.empList.size() - 1); i++) {

                        Boolean comparison = BlockChainKt.verifyBlock(stake, JavaServer.admin.empList.get(i).getLastBlock());

                        consensusList.add(comparison.toString());
                    }

                    //this loop tallies the comparisons between nodes. if the majority of nodes have lists
                    //that do not match the stake, it is denied.
                    int yay = 0;
                    int nay = 0;

                    for(int e = 0; e < consensusList.size(); e++) {

                        if(consensusList.get(e).equals("true")) {
                            yay++;
                        }

                        if(consensusList.get(e).equals("false")) {
                            nay++;
                        }
                    }

                    if(yay > nay) {validity = true;}

                    //either passes the stake to the lotto on true consensus
                    if(validity == true) {
                        JavaServer.admin.tempList.put(name, stake);
                        dos.writeUTF("Stake was accepted as: " + stake);
                    }

                    //or reduces the validator to zero tokens on false consensus
                    else if(validity == false) {
                        dos.writeUTF("Malicious block detected! Your tokens have been confiscated.");
                        dos.writeUTF(stake.printString());
                        emp.changeTokens(0);
                    }

                    consensusList.clear();
                }

                //command to have a client attempt to steal the most recent block in the chain 
                if(received.equals("3")) {

                    dos.writeUTF("Fetching most recently forged block..." + "\n");
                    dos.writeUTF(this.emp.getLastBlock().printString());

                    dos.writeUTF("Changing data to reflect you as owner of block...");
                    this.emp.getLastBlock().changeData("Owner: " + name);

                    dos.writeUTF(this.emp.getLastBlock().printString());
                }

                //tries to spend a coin.
                if(received.equals("4")) {

                    Boolean noCoins = true;
                    //try to spend a block, traverses chain looking for blocks that this employee owns
                    for(int f = 0; f < emp.getEmployeeChain().size(); f++) {

                        //this if statement first checks to see if the employee chain reflects ownership of a block for spending.
                        //if the block is theirs, it checks to see if the block was already spent
                        //if the block has not been spent, it then verifies that the block has is valid against the admin ledger
                        if(emp.getEmployeeChain().get(f).getBlockData().equals("Owner: " + this.name) &&
                                JavaServer.admin.isNotSpent(emp.getEmployeeChain().get(f).getBlockHash()) &&
                                emp.getEmployeeChain().get(f).getBlockData().equals(JavaServer.admin.blockChain.get(f).getBlockData())) {

                            //cashes the block in with the admin
                            JavaServer.admin.spentCoins.add((Block) emp.getEmployeeChain().get(f).clone());
                            dos.writeUTF("You have spent block: " + emp.getEmployeeChain().get(f).getBlockHash() + " for an employee incentive!");
                            noCoins = false;
                            break;
                        }
                    }

                    if(noCoins == true) {
                        dos.writeUTF("You don't seem to have any coins to spend... best get to staking.");
                    }
                }

            } catch (IOException e) {

            }

        }
        try
        {
            // close data streams
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
} 