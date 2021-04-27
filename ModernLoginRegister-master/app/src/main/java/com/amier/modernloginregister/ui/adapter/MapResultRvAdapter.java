package com.amier.modernloginregister.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.amier.modernloginregister.R;
import com.amier.modernloginregister.model.PlacesPOJO;
import com.amier.modernloginregister.model.AddressModel;
import com.amier.modernloginregister.ui.fragment.ResultFragment;

import java.util.List;

public class MapResultRvAdapter extends RecyclerView.Adapter<MapResultRvAdapter.MyViewHolder> {

    private List<PlacesPOJO.CustomA> placesList;
    private List<AddressModel> addressModels;
    ResultFragment.MapResultSelectListener listener;

    public MapResultRvAdapter(List<PlacesPOJO.CustomA> stores, List<AddressModel> addressModels, ResultFragment.MapResultSelectListener listener) {
        this.listener = listener;
        placesList = stores;
        this.addressModels = addressModels;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.store_list_row, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.setData(placesList.get(holder.getAdapterPosition()), holder, addressModels.get(holder.getAdapterPosition()));
        holder.itemView.setOnClickListener(view -> {
            listener.onResultClicked(placesList.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(5, placesList.size());
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtStoreName;
        TextView txtStoreAddr;
        TextView txtStoreDist;
        AddressModel model;

        public MyViewHolder(View itemView) {
            super(itemView);

            this.txtStoreDist = (TextView) itemView.findViewById(R.id.txtStoreDist);
            this.txtStoreName = (TextView) itemView.findViewById(R.id.txtStoreName);
            this.txtStoreAddr = (TextView) itemView.findViewById(R.id.txtStoreAddr);
        }

        public void setData(PlacesPOJO.CustomA info, MyViewHolder holder, AddressModel addressModel) {
            this.model = addressModel;
            holder.txtStoreDist.setText(model.getDistance() + "\n" + model.getDuration());
            holder.txtStoreName.setText(info.name);
            holder.txtStoreAddr.setText(info.vicinity);
        }
    }
}
