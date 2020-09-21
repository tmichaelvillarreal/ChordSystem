package com.example.chordsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NodeAdapter.ItemClickListener {

    private RecyclerView recyclerView;
    private NodeAdapter adapter;
    private List<Node> nodeList = new ArrayList<>();

    private Button addNodeButton;
    private Button lookupButton;
    private EditText lookupKeyEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Chord System");

//        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        String lookupKeyString = sharedPref.getString("lookup_key", "0");
//        final int lookupKey = Integer.parseInt(lookupKeyString);

        lookupKeyEditText = findViewById(R.id.et_mainActivity_lookupKey);
        lookupKeyEditText.setText("0");

        addNodeButton = findViewById(R.id.bt_mainActivity_addNode);
        addNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean hasSpace = false;
                for(int i = 0; i < nodeList.size(); i++) {
                    if(nodeList.get(i).isRealNode() == false) {
                        hasSpace = true;
                    }
                }

                if(hasSpace) { //If the chord system does not have space then we would not want to try to add another node
                    addNode();
                } else {
                    Toast.makeText(MainActivity.this, "Not enough room to add a node", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        lookupButton = findViewById(R.id.bt_mainActivity_lookup);
        lookupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lookupKeyStr = lookupKeyEditText.getText().toString();
                int lookupKey = Integer.parseInt(lookupKeyStr);

                lookup(lookupKey);
            }
        });

        recyclerView = findViewById(R.id.rv_chordSystem);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NodeAdapter(this, this);
        recyclerView.setAdapter(adapter);

        /**
         * The purpose of this for loop is to fill the chord with dummy nodes to then demonstrate how
         * real nodes behave in the chord system.
         * */
        for(int i = 0; i < 16; i++) {
            Node node = new Node(false);
            node.setFakeId(i);
            nodeList.add(node);
        }

        adapter.setNodes(nodeList);
        recyclerView.getLayoutManager().scrollToPosition(Integer.MAX_VALUE / 2);

    }

    /**
     * This method adds a node to the chord system. The way in which the method was written allows a node
     * to be added even if there are 0 nodes in the system.
     * */
    public void addNode() {
        int potentialId = -1;
        boolean conflict = true;

        while(conflict) { //This while loops detect if there are any conflicts with the randomly generated id
            conflict = false;
            potentialId = (int)(Math.random() * 16);

            for(int i = 0; i < nodeList.size(); i++) {
                if(nodeList.get(i).getId() == potentialId) {
                    conflict = true;
                    break;
                }
            }
        }

        int id = potentialId;
        Node newNode = new Node(true); //Creating an actual node
        newNode.setId(id);
        nodeList.remove(id); //We are removing the fake node at this point
        nodeList.add(id, newNode); //Adding the newly created real node to the list of nodes


        /**
         * This for loops looks up the predecessor node for the newly created node. It first goes
         * backwards from where the node is located and marks the first node that it comes across
         * as its predecessor as it should. If there is no node before i reaches 0, another for loop
         * is used that starts at the end of the list and goes backwards from there. The point of
         * that is to demonstrate that a chord system is a circle chain of connections.
         * */
        for(int i = id; i >= 0; i--) {
            if(nodeList.get(i).isRealNode() == true && nodeList.get(i).getId() != id) {
                nodeList.get(id).setPredNode(nodeList.get(i));
                nodeList.get(i).setSuccNode(nodeList.get(id)); //Changes the successor node of a node that already exists
                break;
            }
            if(i == 0) {
                for(int j = nodeList.size() - 1; j >= 0; j--) {
                    if(nodeList.get(j).isRealNode() == true) {
                        nodeList.get(id).setPredNode(nodeList.get(j));
                        nodeList.get(j).setSuccNode(nodeList.get(id));
                        break;
                    }
                }
            }
        }

        /**
         * This for loop is dedicated to finding the successor node for the newly created node.
         * It first starts from the position of the newly created node and goes up from there. If
         * it comes across a node, it makes that node its successor and changes the predecessor of
         * that node to be the new node. If it cannot find a node before the end of the list, then
         * another for loop is used to start at the beginning of the list of nodes and goes up, until
         * a real node is found.
         * */
        for(int i = id; i < nodeList.size(); i++) {
            if(nodeList.get(i).isRealNode() == true && nodeList.get(i).getId() != id) {
                nodeList.get(id).setSuccNode(nodeList.get(i));
                nodeList.get(i).setPredNode(nodeList.get(id));
                break;
            }
            if(i == nodeList.size() - 1) {
                for(int j = 0; j < nodeList.size(); j++) {
                    if(nodeList.get(j).isRealNode() == true) {
                        nodeList.get(id).setSuccNode(nodeList.get(j));
                        nodeList.get(j).setPredNode(nodeList.get(id));
                        break;
                    }
                }
            }
        }

        adapter.setNodes(nodeList);
    }

    /**
     * This method is the method for deleting a node from the chord system and notifying the
     * predecessor and successor nodes of the node being deleted so that their respective successor
     * node and predecessor node can be changed.
     *
     * @param position is the position (in this case, the id) of the node that is to be deleted
     * */
    public void deleteNode(int position) {
        //Predecessor Node needs to change its Successor Node
        nodeList.get(position).getPredNode().setSuccNode(nodeList.get(position).getSuccNode());
        //Successor Node needs to change its Predecessor Node
        nodeList.get(position).getSuccNode().setPredNode(nodeList.get(position).getPredNode());

        nodeList.remove(position);
        Node placeholderNode = new Node(false);
        placeholderNode.setFakeId(position);
        nodeList.add(position, placeholderNode); //Adding a dummy node to the chord system

        adapter.setNodes(nodeList);
    }

    /**
     * This method looks up the succ(k) based on the integer k value given.
     *
     * @param k is the integer key that needs looking up
     * */
    public void lookup(int k) {
        int id = -1;
        String process = "";

        for(int i = 0; i < nodeList.size(); i++) {
            if(nodeList.get(i).isRealNode() == true) { //This will be the first actual node in the chord system
                id = nodeList.get(i).getId();
                break;
            }
        }

        process += "Starting at first node, node id = " + id + "\n";

        boolean found = false;
        Node currentNode = null;

        while(!found) {
            currentNode = nodeList.get(id);
            process += "Checking if current node (Node " + nodeList.get(id).getId() + ") is succ(k)\n";
            if (currentNode.getId() >= k) {
                process += "Current node is succ(k). Found succ(k).\n";
                found = true;
            } else {
                process += "Current node is not succ(k). Moving to the succ(currentNode).\n";
                id = currentNode.getSuccNode().getId();

            }
        }

        process += "Retrieved " + currentNode.getData() + " from Node " + currentNode.getId();
        Toast.makeText(this, "Retrieved " + currentNode.getData() + " from Node " + currentNode.getId(),
                Toast.LENGTH_SHORT).show();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        Intent intent = new Intent(this, LookupResults.class);
        intent.putExtra("process", process);
        startActivity(intent);
    }

    @Override
    public void onItemClick(int position) {
        if(nodeList.get(position).isRealNode() == true) {
            Toast.makeText(this, "Predecessor id: " + nodeList.get(position).getPredNode().getId(),
                    Toast.LENGTH_SHORT).show();

            Toast.makeText(this, "Successor id: " + nodeList.get(position).getSuccNode().getId(),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Not a real node", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onItemLongClick(int position) {
        if(nodeList.get(position).isRealNode() == true) {
            deleteNode(position);
        } else {
            Toast.makeText(this, "Not an actual node to delete", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}