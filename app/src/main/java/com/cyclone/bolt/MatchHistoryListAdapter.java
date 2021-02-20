package com.cyclone.bolt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MatchHistoryListAdapter extends RecyclerView.Adapter<MatchHistoryListAdapter.ViewHolder> {

    ViewHolder viewHolder;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView opponentName;
        public ImageView opponentPicture;

        public ViewHolder(View viewItem) {
            super(viewItem);
            opponentName = viewItem.findViewById(R.id.opponentName);
            opponentPicture = viewItem.findViewById(R.id.opponentPicture);
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
        ImageView opponentPicture = holder.opponentPicture;
        if(match.getOpponent() != null) {
            tv_opponentName.setText((match.getOpponent().name));
            try {
                new MatchHistoryListAdapter.DownloadImageTask(opponentPicture).execute(match.getOpponent().profilePicUrl);
            } catch(Exception e) {e.printStackTrace();}
        }
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error loading picture", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
