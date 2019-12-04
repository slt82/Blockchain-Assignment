package com.blockchain

import java.security.MessageDigest

//com.blockchain.com.blockchain.Block Class
class Block : Cloneable{

    var blockHash : String
    var priorHash : String
    var blockData :String
    var validator : String

    constructor(pHash : String, bData : String, vdtr : String) {

        this.priorHash = pHash

        this.blockData = bData

        this.blockHash = hashCalculator()

        this.validator = vdtr
    }

    //overrides object clone() specifically for the clients that are joining the server before any blocks have been forged
    //so that they can create their own genesis block in their list. prevents separate block objects referencing eachother.
    override public fun clone(): Any{
        return super.clone()
    }
    //function for creating a hash
    fun hashCalculator() : String {return sha256it(priorHash + blockData)
    }

    //function to manipulate block chain
    fun changeData(newData: String){

        //changes the data value of a block and recalculates hash
        blockData = newData
        blockHash = hashCalculator()
    }

    //prints out this block data
    fun printString(): String{

        return("-------------------------------" + "\n" +
                "|[1] Prior Hash: " + priorHash + "\n" +
                "|[2] Current Hash: " + blockHash + "\n" +
                "|[3] com.blockchain.com.blockchain.Block Data: " + blockData + "\n" +
                "-------------------------------" + "\n")
    }
}
//--------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------

//com.blockchain.com.blockchain.Employee class deployed by clienthandler
class Employee{

    val employeeID: String
    val employeeChain: ArrayList<Block>
    var tokens: Int

    constructor(id:String, tokes: Int){

        employeeID = id
        tokens = tokes
        employeeChain = ArrayList<Block>()
    }

    //--------------------------------------------------------------------------------------------------
    fun changeTokens(input: Int){
        tokens = input
    }
    //--------------------------------------------------------------------------------------------------
    fun fetchTokens(): Int{return tokens}
    //--------------------------------------------------------------------------------------------------
    fun getID(): String{return employeeID}
    //--------------------------------------------------------------------------------------------------
    fun generateStakeBlock(data: String, validator: String) : Block {


        val lastBlock: Int = (employeeChain.size-1)
        val previousBlock: Block = employeeChain[lastBlock]
        val stakeBlock = Block(previousBlock.blockHash, data, validator)

        return stakeBlock
    }
    //--------------------------------------------------------------------------------------------------
    fun getLastBlock(): Block {

        val lastBlock: Int = (employeeChain.size-1)
        val previousBlock: Block = employeeChain[lastBlock]

        return previousBlock
    }

    //--------------------------------------------------------------------------------------------------
    fun prettyBlockChain(): String{

        val prettyChain: StringBuilder = StringBuilder()

        for(i in 0..(employeeChain.size - 1)){

            prettyChain.append("-------------------------------" + "\n" +
                    "| com.blockchain.com.blockchain.Block " + i + "\n"+
                    "| Prior Hash: " + employeeChain[i].priorHash + "\n" +
                    "| Current Hash: " + employeeChain[i].blockHash + "\n" +
                    "| com.blockchain.com.blockchain.Block Data: " + employeeChain[i].blockData + "\n" +
                    "| Initial Validator: " + employeeChain[i].validator + "\n" +
                    "-------------------------------" + "\n")
        }

        return prettyChain.toString()
    }
}

//--------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------

//this method generates a random number. spoofs collectin an employee's tokens from a database
fun collectTokens(): Int{return (0..50).random()}
//--------------------------------------------------------------------------------------------------
//sha256 digester for hash function in block
fun sha256it(toHash: String): String{

    val bytes = toHash.toByteArray()
    val digest = MessageDigest.getInstance("SHA-256")
    val digested = digest.digest(bytes)
    return digested.toString()
}
//--------------------------------------------------------------------------------------------------

//function to verify a block's integrity. this is meant to work in conjunction with the consensus validation
fun verifyBlock(currentBlock: Block, previousBlock: Block): Boolean{

    if(!previousBlock.blockHash.equals(currentBlock.priorHash)){

        return false
    }

    return true
}
//--------------------------------------------------------------------------------------------------
