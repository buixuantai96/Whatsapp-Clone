package won.kma.buitai.whatsapp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import won.kma.buitai.whatsapp.ChatActivity;
import won.kma.buitai.whatsapp.helper.VirgilHelper;
import won.kma.buitai.whatsapp.model.Contacts;
import won.kma.buitai.whatsapp.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View PrivateChatsView;
    private RecyclerView chatsList;
    private String currentUserID;

    private DatabaseReference ChatsRef, UserRef;
    private FirebaseAuth firebaseAuth;

    private VirgilHelper virgilHelper =  new VirgilHelper();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        chatsList = (RecyclerView)PrivateChatsView.findViewById(R.id.recycleview_chat_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return PrivateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions <Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(ChatsRef, Contacts.class).build();

        FirebaseRecyclerAdapter <Contacts, ChatViewHolders> adapter = new FirebaseRecyclerAdapter<Contacts, ChatViewHolders>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolders holder, int position, @NonNull Contacts model) {
                final String usersID = getRef(position).getKey();

                final String[] retrieveImage = {"default_image"};

                UserRef.child(usersID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(dataSnapshot.hasChild("image")){
                                retrieveImage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retrieveImage[0]).placeholder(R.drawable.profile_image)
                                        .into(holder.profileImage);
                            }
                            final String retrieveName = dataSnapshot.child("name").getValue().toString();
                            final String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                            holder.userName.setText(retrieveName);
                            holder.userStatus.setText("Last seen: " + "\n" + "Date " + "Time");
                            //show last seen (state)
                            if (dataSnapshot.child("userState").hasChild("state")){
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                if (state.equals("online")){
                                    holder.userStatus.setText("online");
                                }
                                else if(state.equals("offline")){
                                    holder.userStatus.setText("Last seen: " + date + " " + time);
                                }
                            }
                            else{
                                holder.userStatus.setText("offline");
                            }

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    virgilHelper.findPublicKey(usersID);

                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visitorUserID", usersID);
                                    chatIntent.putExtra("visitorUserName", retrieveName);
                                    chatIntent.putExtra("visitorImageProfile", retrieveImage[0]);
                                    startActivity(chatIntent);
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatViewHolders onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout, viewGroup, false);
                return new ChatViewHolders(view);
            }
        };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class ChatViewHolders extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView userStatus, userName;


        public ChatViewHolders(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.image_users_profile);
            userName = itemView.findViewById(R.id.tv_profile_name);
            userStatus = itemView.findViewById(R.id.tv_profile_status);
        }
    }
}
