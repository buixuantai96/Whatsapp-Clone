package won.kma.buitai.whatsapp.adapter;


import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import won.kma.buitai.whatsapp.model.Messages;
import won.kma.buitai.whatsapp.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List< Messages> userMessagesList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;


    public MessageAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = (TextView) itemView.findViewById(R.id.tv_sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.tv_receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.img_message_profile);


        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        firebaseAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        String messageSenderID = firebaseAuth.getCurrentUser().getUid();
        Messages messagesText = userMessagesList.get(i);

        String fromUserID =  messagesText.getFrom();
        String fromMessageType = messagesText.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

       usersRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (dataSnapshot.hasChild("image")){
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                   Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image)
                           .into(messageViewHolder.receiverProfileImage);
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

       if (fromMessageType.equals("text")){
           messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
           messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);
           messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);
           if (fromUserID.equals(messageSenderID)){
               messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messagesText.getMessage());
           }
           else{
               messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
               messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

               messageViewHolder.receiverProfileImage.setBackgroundResource(R.drawable.sender_message_layout);
               messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
               messageViewHolder.receiverMessageText.setText(messagesText.getMessage());
           }
       }

    }

    @Override
    public int getItemCount() {

        return userMessagesList.size();
    }


}
