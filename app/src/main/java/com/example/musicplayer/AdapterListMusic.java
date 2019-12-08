package com.example.musicplayer;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.lang.ref.WeakReference;
import java.util.List;

class AdapterListMusic extends RecyclerView.Adapter<AdapterListMusic.MusicViewHolder> {
    private Context mContext;
    private static List<MusicModel> mListMusic;
    private static GoToPlayActivityInterface mGoToPlayActivityInterface;
    AdapterListMusic(Context context, List<MusicModel> listMusic) {
        mListMusic = listMusic;
        mContext = context;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_music_model, parent, false);
        return new MusicViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MusicViewHolder holder, int position) {

        if (holder.getAdapterPosition() != -1){
            holder.tv_name.setText(mListMusic.get(holder.getAdapterPosition()).getArtist());
            holder.tv_details.setText(mListMusic.get(holder.getAdapterPosition()).getTitle());
            if (mListMusic.get(holder.getAdapterPosition()).getData()==null){
                mListMusic.get(holder.getAdapterPosition()).setData(new MediaMetadataRetriever());
                // for increase performance of load cover images in RecyclerView
                holder.setThumb(holder, holder.getAdapterPosition());
            }
            else {
                Glide.with(mContext)
                        .load(mListMusic.get(holder.getAdapterPosition()).getData().getEmbeddedPicture())
                        .placeholder(R.drawable.default_music_avatar)
                        .into(holder.iv_thumbnail);
            }

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoToPlayActivityInterface!=null){
                    mGoToPlayActivityInterface.sendPlayInformation(mListMusic.get(holder.getAdapterPosition()).getTag());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListMusic.size();
    }
    
    class MusicViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name;
        private TextView tv_details;
        private ImageView iv_thumbnail;
        MusicViewHolder(View view) {
            super(view);
            tv_name = view.findViewById(R.id.tv_name);
            tv_details = view.findViewById(R.id.tv_details);
            iv_thumbnail = view.findViewById(R.id.iv_thumbnail);
        }

        private void setThumb(MusicViewHolder holder, int position){
            new MyAsync(holder, position, mContext).execute();
        }
    }


    static class MyAsync extends AsyncTask<Void, Void, Void> {
        private MusicViewHolder holder;
        private int position;
        private WeakReference<Context> context;

        MyAsync(MusicViewHolder holder, int position, Context context) {
            this.holder = holder;
            this.position = position;
            this.context = new WeakReference<>(context);
        }
        @Override
        protected Void doInBackground(Void... voids) {
            mListMusic.get(position).getData().setDataSource(mListMusic.get(position).getFilePath());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Glide.with(context.get())
                    .load(mListMusic.get(position).getData().getEmbeddedPicture())
                    .placeholder(R.drawable.default_music_avatar)
                    .into(holder.iv_thumbnail);
        }
    }

    interface GoToPlayActivityInterface{
        void sendPlayInformation(int tag);
    }

    static void setGoToPlayActivityInterface(GoToPlayActivityInterface goToPlayActivityInterface){
        mGoToPlayActivityInterface = goToPlayActivityInterface;
    }
}