package won.kma.buitai.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import won.kma.buitai.whatsapp.model.Contacts;

public class FindFriendActivity extends AppCompatActivity {

    private Toolbar toolbarFindFriend;
    private RecyclerView recyclerListFindFriend;
    private DatabaseReference UserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerListFindFriend = (RecyclerView) findViewById(R.id.recycleview_findfriend);
        recyclerListFindFriend.setLayoutManager(new LinearLayoutManager(this));

        toolbarFindFriend = (Toolbar) findViewById(R.id.appBarLayout_findfriend);
        setSupportActionBar(toolbarFindFriend);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friend");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions
                .Builder<Contacts>()
                .setQuery(UserReference, Contacts.class).build();

//get data using holder object using adapter
        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                        holder.textViewUserName.setText(model.getName());
                        holder.textViewUserStatus.setText(model.getStatus());
                        //.placeholder set image default if image not available in database
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.imageUserProfileImage);
                        //Firebase Recycler Adapter onclick item listener - Handle item clicks
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                String visit_user_id = getRef(position).getKey();

                                Intent profileIntent = new Intent(FindFriendActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        //connect viewholder to layout
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout, viewGroup, false);
                        FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                        return viewHolder;
                    }
                };

        recyclerListFindFriend.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserName, textViewUserStatus;
        CircleImageView imageOnline, imageUserProfileImage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.tv_profile_name);
            textViewUserStatus = itemView.findViewById(R.id.tv_profile_status);
            imageOnline = itemView.findViewById(R.id.image_online);
            imageUserProfileImage = itemView.findViewById(R.id.image_users_profile);
        }
    }
}
