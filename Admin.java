package com.blockchain;

import java.util.*;
import java.util.Map.Entry;

//the admin class. acts as the employer administrator of the server in this case. facilitates
//node communication and picks a winning validator/accepts purchases using coins
public class Admin implements Runnable{

    ArrayList<Block> blockChain;
    Map<String, Block> tempList;
    ArrayList<Employee> empList;
    Scanner adminScan;
    Block genesisBlock;
    ArrayList<Block> spentCoins;

    public Admin(Scanner scan) {

        blockChain = new ArrayList<Block>();
        tempList = new HashMap<String, Block>();
        empList = new ArrayList<Employee>();
        spentCoins = new ArrayList<Block>();
        adminScan = scan;

    }

    //method for the admin to match an eomployee ID to a token balance
    public int matchIDtoTokens(String id) {

        for(int a = 0; a < empList.size(); a++) {

            if(empList.get(a).getID().equals(id)) {
                return empList.get(a).fetchTokens();
            }
        }
        return 0;
    }

    //tests to see if a block has already been spent and added to the spent blocks list
    public Boolean isNotSpent(String hash) {

        for(int g = 0; g < spentCoins.size(); g++) {

            if(spentCoins.get(g).getBlockHash().equals(hash)) {

                return false;
            }
        }

        return true;
    }

    public void run() {

        //the first block in the chain
        genesisBlock = new Block("genesis", "genesis", "genesis");
        blockChain.add(genesisBlock);

        while(true) {

            String adminInput = adminScan.nextLine();

            //server command to select a winner from active stakes in the pool.
            if(adminInput.equals("selectWinner")) {

                System.out.println("Selecting a winner from the participating validators...");

                LinkedList<String> lottery = new LinkedList<String>();
                Iterator<Entry<String, Block>> mapIterator = tempList.entrySet().iterator();

                //iterate through the map of validators and generate an entry into the lotto pool for each token they have
                while(mapIterator.hasNext()) {

                    Map.Entry<String, Block> mapComponent = (Map.Entry<String, Block>)mapIterator.next();
                    int tokenCount = (matchIDtoTokens(mapComponent.getKey()));

                    for(int b = 0; b <= tokenCount; b++) {

                        lottery.add(mapComponent.getKey());
                    }
                }

                //selects a random number to be the index of the winner in the lotto list.
                Random winner = new Random();
                int winningIndex = winner.nextInt(lottery.size());

                //finds the winning ID from the lotto
                String winningID = lottery.get(winningIndex);

                System.out.println("And the winner is... com.blockchain.com.blockchain.Employee " + winningID);

                //adds the winning block to the server chain via the value assigned to the winning ID key in hash map
                blockChain.add((Block) tempList.get(winningID).clone());

                //adds the new block to all the clients lists
                for(int c = 0; c < empList.size(); c++) {

                    empList.get(c).getEmployeeChain().add((Block) tempList.get(winningID).clone());
                }

                //resets the temporary lists for next bidding cycle
                tempList.clear();
                lottery.clear();
            }
        }
    }
}
