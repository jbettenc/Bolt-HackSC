package com.cyclone.bolt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MatchHistoryListAdapter extends RecyclerView.Adapter<MatchHistoryListAdapter.ViewHolder> {

    ViewHolder viewHolder;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView opponentName;

        public ViewHolder(View viewItem) {
            super(viewItem);
            opponentName = viewItem.findViewById(R.id.opponentName);
        }
    }

    private List<Match> matches;

    public MatchHistoryListAdapter() {
        this.matches = new ArrayList<>();
    }
    public MatchHistoryListAdapter(List<Match> matches) {
        this.matches = matches;
    }

    public void setMatch(Match match) {
        boolean found = false;
        for(int i = 0; i < matches.size(); i++) {
            if(match.getMatchId().equals(matches.get(i).getMatchId())) {
                matches.set(i, match);
                found = true;
            }
        }
        if(!found)
            matches.add(match);
        this.notifyDataSetChanged();
    }

    @Override
    public MatchHistoryListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View matchesView = inflater.inflate(R.layout.match_list_layout, parent, false);

        viewHolder = new ViewHolder(matchesView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MatchHistoryListAdapter.ViewHolder holder, int position) {
        Match match = matches.get(position);
        TextView tv_opponentName = holder.opponentName;
        if(match.getOpponent() != null)
            tv_opponentName.setText((match.getOpponent().name));
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }
}
