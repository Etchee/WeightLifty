package etchee.com.weightlifty.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import etchee.com.weightlifty.R;

/**
 * The welcome activity fragment
 * Created by rikutoechigoya on 2017/05/11.
 */

public class MainActivityFragment extends android.support.v4.app.Fragment {

    private Button begin_workout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //"Begin Workout" button to launch listActivity
        begin_workout = (Button) view.findViewById(R.id.begin_workout_button);
        begin_workout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WorkoutListActivity.class);
                startActivity(intent);
            }
        });
    }
}
