package com.rocket.biometrix.Settings;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.rocket.biometrix.Login.LocalAccount;
import com.rocket.biometrix.Login.SettingKeys;
import com.rocket.biometrix.NavigationDrawerActivity;
import com.rocket.biometrix.R;

import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ModuleSettings.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ModuleSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ModuleSettings extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Switch moodSwitch;
    private Switch sleepSwitch;
    private Switch exerciseSwitch;
    private Switch dietSwitch;
    private Switch medicationSwitch;

    public ModuleSettings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ModuleSettings.
     */
    // TODO: Rename and change types and number of parameters
    public static ModuleSettings newInstance(String param1, String param2) {
        ModuleSettings fragment = new ModuleSettings();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        try{
            NavigationDrawerActivity nav = (NavigationDrawerActivity) getActivity();
            //Change the title of the action bar to reflect the current fragment
            nav.setActionBarTitleFromFragment(R.string.action_bar_title_module_settings);
            //set activities active fragment to this one
            nav.activeFragment = this;
        } catch (Exception e){}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_module_settings, container, false);
        // Inflate the layout for this fragment

        //Grab reference to local account
        LocalAccount localAccount = LocalAccount.GetInstance();

        //Grab reference to the switch and set it accordingly
        boolean moodCheck = localAccount.getBoolean(view.getContext(), SettingKeys.MOOD_MODULE, true);
        moodSwitch = ((Switch) view.findViewById(R.id.DisableSwitchMoodModule));
        moodSwitch.setChecked(moodCheck);

        boolean sleepCheck = localAccount.getBoolean(view.getContext(), SettingKeys.SLEEP_MODULE, true);
        sleepSwitch = ((Switch) view.findViewById(R.id.DisableSwitchSleepModule));
        sleepSwitch.setChecked(sleepCheck);

        boolean exerciseCheck = localAccount.getBoolean(view.getContext(), SettingKeys.EXERCISE_MODULE, true);
        exerciseSwitch = ((Switch) view.findViewById(R.id.DisableSwitchExerciseModule));
        exerciseSwitch.setChecked(exerciseCheck);

        boolean dietCheck = localAccount.getBoolean(view.getContext(), SettingKeys.DIET_MODULE, true);
        dietSwitch = ((Switch) view.findViewById(R.id.DisableSwitchDietModule));
        dietSwitch.setChecked(dietCheck);

        boolean medicationCheck = localAccount.getBoolean(view.getContext(), SettingKeys.MEDICATION_MODULE, true);
        medicationSwitch = ((Switch) view.findViewById(R.id.DisableSwitchMedicationModule));
        medicationSwitch.setChecked(medicationCheck);
        return view;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void onAcceptClick(View v)
    {
        LocalAccount localAccount = LocalAccount.GetInstance();

        boolean moodChecked = moodSwitch.isChecked();
        boolean sleepChecked = sleepSwitch.isChecked();
        boolean exerciseChecked = exerciseSwitch.isChecked();
        boolean dietChecked = dietSwitch.isChecked();
        boolean medicationChecked = medicationSwitch.isChecked();

        localAccount.setBoolean(v.getContext(), SettingKeys.MOOD_MODULE, moodChecked);
        localAccount.setBoolean(v.getContext(), SettingKeys.SLEEP_MODULE, sleepChecked);
        localAccount.setBoolean(v.getContext(), SettingKeys.EXERCISE_MODULE, exerciseChecked);
        localAccount.setBoolean(v.getContext(), SettingKeys.DIET_MODULE, dietChecked);
        localAccount.setBoolean(v.getContext(), SettingKeys.MEDICATION_MODULE, medicationChecked);
    }
}
