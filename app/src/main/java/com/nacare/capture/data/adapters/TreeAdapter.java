package com.nacare.capture.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nacare.capture.R;
import com.nacare.capture.data.model.OrgTreeNode;

import java.util.List;

public class TreeAdapter extends RecyclerView.Adapter<TreeAdapter.TreeViewHolder> {

    private final Context context;
    private final List<OrgTreeNode> treeNodes;
    private final OnItemClickListener click;
    private boolean isChecked;

    public TreeAdapter(Context context, List<OrgTreeNode> treeNodes, OnItemClickListener click) {
        this.context = context;
        this.treeNodes = treeNodes;
        this.click = click;
    }


    public interface OnItemClickListener {
        void onItemClick(OrgTreeNode orgTreeNode);
    }

    public class TreeViewHolder extends RecyclerView.ViewHolder {
        public TreeViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public TreeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tree_item_layout, parent, false);
        return new TreeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TreeViewHolder holder, int position) {
        OrgTreeNode node = treeNodes.get(position);
        View itemView = holder.itemView;

        // Adjust the indentation based on the tree level
        int paddingLeft = 20 * getTreeLevel(node);
        itemView.setPadding(paddingLeft, 0, 0, 0);

        // Set label text
        TextView labelTextView = itemView.findViewById(R.id.org_unit_name);
        labelTextView.setText(node.getLabel());

        // Handle click to expand/collapse
        itemView.setOnClickListener(v -> toggleNode(position, holder));

        // Handle visibility based on whether the node has children and is expanded
        ImageView arrowIcon = itemView.findViewById(R.id.org_unit_icon);
        arrowIcon.setVisibility(node.getChildren().isEmpty() ? View.INVISIBLE :
                (node.getLevel().equals("5") ? View.INVISIBLE : View.VISIBLE));

        // Set label text
        CheckBox checkbox = itemView.findViewById(R.id.checkbox);
        checkbox.setVisibility(node.getChildren().isEmpty() ? (node.getLevel().equals("5") ? View.VISIBLE : View.INVISIBLE) : View.INVISIBLE);

        // Handle visibility based on whether the node has children and is expanded
        if (node.getChildren().isEmpty()) {
            checkbox.setOnClickListener(v -> {
                OrgTreeNode org = treeNodes.get(position);
                if (org.getLevel().equals("5")) {
                    click.onItemClick(org);
                }
            });
        }

        // Handle expand/collapse state
        arrowIcon.setImageResource(node.isExpanded() ? R.drawable.baseline_remove_circle_24 : R.drawable.ic_add_circle);

        // Set up child RecyclerView
        RecyclerView childRecyclerView = itemView.findViewById(R.id.childRecyclerView);
        TreeAdapter childAdapter = new TreeAdapter(context, node.getChildren(), click);
        childRecyclerView.setAdapter(childAdapter);
        childRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Set child RecyclerView visibility based on the expand/collapse state
        childRecyclerView.setVisibility(node.isExpanded() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return treeNodes.size();
    }

    private int getTreeLevel(OrgTreeNode node) {
        int level = 0;
        OrgTreeNode currentNode = node;
        while (!currentNode.getChildren().isEmpty()) {
            level++;
            currentNode = currentNode.getChildren().get(0);
        }
        return level;
    }

    private void toggleNode(int position, TreeViewHolder holder) {
        OrgTreeNode node = treeNodes.get(position);
        node.setExpanded(!node.isExpanded());
        notifyItemChanged(position);

        // Update child RecyclerView visibility
        RecyclerView childRecyclerView = holder.itemView.findViewById(R.id.childRecyclerView);
        childRecyclerView.setVisibility(node.isExpanded() ? View.VISIBLE : View.GONE);
    }
}
