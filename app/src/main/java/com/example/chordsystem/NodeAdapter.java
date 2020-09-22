package com.example.chordsystem;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.NodeViewHolder> {

    private List<Node> mNodes;
    private Context mContext;
    private ItemClickListener mClickListener;

    public NodeAdapter(Context context, ItemClickListener itemClickListener) {
        mContext = context;
        mClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public NodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.rv_listitem_chordsystem, parent,
                false);
        return new NodeViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull NodeViewHolder holder, int position) {
        int positionInList = position % mNodes.size();
        Node currentNode = mNodes.get(positionInList);
        if(currentNode.isRealNode() == true) {
            holder.nodeIdentifier.setText(currentNode.getId() + "");
            holder.nodeIdentifier.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
            holder.realNode.setTextColor(mContext.getResources().getColor(android.R.color.black));
        } else {
            holder.nodeIdentifier.setText(currentNode.getFakeId() + "");
            holder.nodeIdentifier.setTextColor(mContext.getResources().getColor(R.color.fakeNode));
            holder.realNode.setTextColor(mContext.getResources().getColor(R.color.fakeNode));
        }

        //holder.realNode.setText(mNodes.get(position).isRealNode() + "");
    }

    @Override
    public int getItemCount() {
        if (mNodes == null) return 0;
        return Integer.MAX_VALUE;
    }



    public void setNodes(List<Node> nodes) {
        mNodes = nodes;
        notifyDataSetChanged();
    }

    class NodeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView nodeIdentifier;
        private TextView realNode;

        public NodeViewHolder(@NonNull View itemView) {
            super(itemView);

            nodeIdentifier = itemView.findViewById(R.id.rv_listItem_identifier);
            realNode = itemView.findViewById(R.id.rv_listItem_nodeTitle);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onItemClick(getAdapterPosition() % 16);
        }

        @Override
        public boolean onLongClick(View view) {
            mClickListener.onItemLongClick(getAdapterPosition() % 16);
            return true;
        }
    }

    public interface ItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }
}
