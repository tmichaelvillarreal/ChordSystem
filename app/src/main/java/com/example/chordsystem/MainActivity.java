package com.example.chordsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
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

    String process = "";
    List<Integer> listOfVisitedNotes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Chord System");

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
                if(lookupKey >= 0 && lookupKey <= 15) {
                    lookupK(lookupKey);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 0; i < listOfVisitedNotes.size(); i++) {
                                Node testNode = nodeList.get(listOfVisitedNotes.get(i));
                                testNode.setBeingLookedAt(true);
                                nodeList.remove(testNode.getId());
                                nodeList.add(testNode.getId(), testNode);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.setNodes(nodeList);
                                    }
                                });

                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException ex) {

                                }

                                testNode.setBeingLookedAt(false);
                                nodeList.remove(testNode.getId());
                                nodeList.add(testNode.getId(), testNode);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.setNodes(nodeList);
                                    }
                                });

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {

                                }
                            }

                            Intent intent = new Intent(MainActivity.this, LookupResults.class);
                            intent.putExtra("process", process);
                            startActivity(intent);
                        }
                    }).start();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid number entered. Only 0 - 15 are valid",
                            Toast.LENGTH_SHORT).show();
                }



            }
        });

        recyclerView = findViewById(R.id.rv_chordSystem);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
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

        int startingId = (int)(Math.random() * 16);
        Node startNode = new Node(true);
        startNode.setId(startingId);
        startNode.setSuccNode(startNode);
        startNode.setPredNode(startNode);

        nodeList.remove(startingId);
        nodeList.add(startingId, startNode);

        adapter.setNodes(nodeList);
        recyclerView.getLayoutManager().scrollToPosition(Integer.MAX_VALUE / 2);

        AlertDialog.Builder mAlert = new AlertDialog.Builder(MainActivity.this);
        mAlert.setTitle("Deleting Nodes");
        mAlert.setMessage("In order to delete a node, simply click on a node and hold until the node is deleted.");

        mAlert.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        mAlert.show();

    }

    /**
     * Method to add a node to the chord system making use of the lookup algorithm
     * */
    public void addNode() {

        boolean checkIfEmpty = true;
        for(int i = 0; i < nodeList.size(); i++) {
            if(nodeList.get(i).isRealNode() == true) {
                checkIfEmpty = false;
            }
        }

        if(checkIfEmpty) {
            int startingId = (int)(Math.random() * 16);
            Node startNode = new Node(true);
            startNode.setId(startingId);
            startNode.setSuccNode(startNode);
            startNode.setPredNode(startNode);

            nodeList.remove(startingId);
            nodeList.add(startingId, startNode);
            return;
        }

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
        Node newNode = new Node(true);
        newNode.setId(id);

        Node succId = lookup(id);

        newNode.setSuccNode(succId);
        newNode.setPredNode(succId.getPredNode());

        succId.getPredNode().setSuccNode(newNode);
        succId.setPredNode(newNode);

        nodeList.remove(id);
        nodeList.add(id, newNode);

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
    public void lookupK(int k) {
        int id = -1;
        process = "";

        for(int i = 0; i < nodeList.size(); i++) {
            if(nodeList.get(i).isRealNode() == true) { //This will be the first actual node in the chord system
                id = nodeList.get(i).getId();
                break;
            }
        }

        process += "Starting at first node, node id = " + id + "\n";

        boolean found = false;
        Node succNode = null;
        int prevId = id;

        while(!found) {
            succNode = nodeList.get(id);
            listOfVisitedNotes.add(id);
            process += "Checking if current node (Node " + nodeList.get(id).getId() + ") is succ(k)\n";
            if (succNode.getId() >= k || prevId > id) {
                process += "Current node is succ(k). Found succ(k).\n";
                found = true;
            } else {
                process += "Current node is not succ(k). Moving to the succ(currentNode).\n";
                prevId = id;
                id = succNode.getSuccNode().getId();
            }
        }

        process += "Retrieved " + succNode.getData() + " from Node " + succNode.getId();

    }

    private Node lookup(int nodeId) {
        int id = -1;

        for(int i = 0; i < nodeList.size(); i++) {
            if(nodeList.get(i).isRealNode() == true) { //This will be the first actual node in the chord system
                id = nodeList.get(i).getId();
                break;
            }
        }

        boolean found = false;
        Node succNode = null;
        int prevId = -1;

        while(!found) {
            succNode = nodeList.get(id);
            if (succNode.getId() > nodeId || prevId >= id) {
                found = true;
            } else {
                prevId = id;
                id = succNode.getSuccNode().getId();

            }
        }

        return succNode;
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