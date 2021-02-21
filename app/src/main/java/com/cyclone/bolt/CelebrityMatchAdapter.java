package com.cyclone.bolt;

import android.content.Context;
import android.content.Intent;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CelebrityMatchAdapter extends RecyclerView.Adapter<CelebrityMatchAdapter.ViewHolder> {

    CelebrityMatchAdapter.ViewHolder viewHolder;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView opponentName;
        public ImageView opponentPicture;

        public ViewHolder(View viewItem) {
            super(viewItem);
            opponentName = viewItem.findViewById(R.id.opponentName);
            opponentPicture = viewItem.findViewById(R.id.opponentPicture);
        }
    }

    public static List<Match> matches;

    public CelebrityMatchAdapter() {
        this.matches = new ArrayList<>();
    }
    public CelebrityMatchAdapter(List<Match> matches) {
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
    public CelebrityMatchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View matchesView = inflater.inflate(R.layout.match_list_layout, parent, false);

        viewHolder = new ViewHolder(matchesView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CelebrityMatchAdapter.ViewHolder holder, int position) {
        Match match = matches.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), MatchDetail.class);
                intent.putExtra("matchNumber", position);
                holder.itemView.getContext().startActivity(intent);
            }
        });
        TextView tv_opponentName = holder.opponentName;
        ImageView opponentPicture = holder.opponentPicture;
        if(match.getOpponent() != null) {
            tv_opponentName.setText((match.getOpponent().name));
            try {
                new CelebrityMatchAdapter.DownloadImageTask(opponentPicture).execute(match.getOpponent().profilePicUrl);
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
