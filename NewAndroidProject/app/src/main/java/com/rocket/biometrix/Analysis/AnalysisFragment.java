package com.rocket.biometrix.Analysis;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.rocket.biometrix.Database.LocalStorageAccess;
import com.rocket.biometrix.Database.LocalStorageAccessMood;
import com.rocket.biometrix.Database.LocalStorageAccessSleep;
import com.rocket.biometrix.Login.SettingsHelper;
import com.rocket.biometrix.NavigationDrawerActivity;
import com.rocket.biometrix.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AnalysisFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AnalysisFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnalysisFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private View analysisView;

    private int daysApart;

    public AnalysisFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment AnalysisFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AnalysisFragment newInstance() {
        return new AnalysisFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            NavigationDrawerActivity nav = (NavigationDrawerActivity) getActivity();
            //Change the title of the action bar to reflect the current fragment
            nav.setActionBarTitleFromFragment(R.string.action_bar_title_analysis);
            //set activities active fragment to this one
            nav.activeFragment = this;
        } catch (Exception e){}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);
        analysisView = view;

        List<String> stringList = SettingsHelper.getEnabledModuleNames(view.getContext());
        String[] moduleNames = stringList.toArray(new String[stringList.size()]);
        ArrayAdapter<String> spinnerArrayAdapterTable = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, moduleNames);
        ((Spinner)view.findViewById(R.id.analysisTableSpinner1)).setAdapter(spinnerArrayAdapterTable);
        ((Spinner)view.findViewById(R.id.analysisTableSpinner2)).setAdapter(spinnerArrayAdapterTable);

        //Sets up the spinner to point, and also makes it point to the middle element, which should
        //be zero
        ArrayAdapter daySpinnerAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.analysis_day_diff_spinner, android.R.layout.simple_spinner_item);

        ((Spinner)view.findViewById(R.id.analysisDateDiffSpinner)).setAdapter(daySpinnerAdapter);
        ((Spinner)view.findViewById(R.id.analysisDateDiffSpinner)).setSelection(7);

        updateColumnSpinners();

        //Update table spinners when selected
        ((Spinner)view.findViewById(R.id.analysisTableSpinner1)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateColumnSpinners();
                updateTextView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ((Spinner)view.findViewById(R.id.analysisTableSpinner2)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateColumnSpinners();
                updateTextView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ((Spinner)view.findViewById(R.id.analysisColumnSpinner1)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTextView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ((Spinner)view.findViewById(R.id.analysisColumnSpinner2)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTextView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ((Spinner)view.findViewById(R.id.analysisDateDiffSpinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTextView();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Updates the spinners for the columns depending on which module is in the table
     */
    private void updateColumnSpinners()
    {
        String tableName1 = ((Spinner) analysisView.findViewById(R.id.analysisTableSpinner1)).getSelectedItem().toString();
        String tableName2 = ((Spinner) analysisView.findViewById(R.id.analysisTableSpinner2)).getSelectedItem().toString();

        List<String> stringList = SettingsHelper.getEnabledAnalysisColumns(analysisView.getContext(), tableName1, true);
        String[] columnNames1 = stringList.toArray(new String[stringList.size()]);
        stringList = SettingsHelper.getEnabledAnalysisColumns(analysisView.getContext(), tableName2, true);
        String[] columnNames2 = stringList.toArray(new String[stringList.size()]);

        ArrayAdapter<String> spinnerArrayAdapterColumn1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, columnNames1);
        ArrayAdapter<String> spinnerArrayAdapterColumn2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, columnNames2);
        ((Spinner) analysisView.findViewById(R.id.analysisColumnSpinner1)).setAdapter(spinnerArrayAdapterColumn1);
        ((Spinner) analysisView.findViewById(R.id.analysisColumnSpinner2)).setAdapter(spinnerArrayAdapterColumn2);
    }

    /**
     * Updates the textview to show the operation that is being done when the user clicks run analysis
     */
    private void updateTextView()
    {
        StringBuilder operationDisplay = new StringBuilder();
        operationDisplay.append("Correlating ");
        if (((Spinner) analysisView.findViewById(R.id.analysisColumnSpinner1)).getSelectedItem() != null) {
            operationDisplay.append(((Spinner) analysisView.findViewById(R.id.analysisColumnSpinner1)).getSelectedItem().toString());
        }
        else
        {
            operationDisplay.append("Nothing");
        }

        operationDisplay.append(" with ");
        if (((Spinner) analysisView.findViewById(R.id.analysisColumnSpinner2)).getSelectedItem() != null) {
            operationDisplay.append(((Spinner) analysisView.findViewById(R.id.analysisColumnSpinner2)).getSelectedItem().toString());
        }
        else
        {
            operationDisplay.append("Nothing");
        }

        Integer daysApart = Integer.parseInt(((Spinner)analysisView.findViewById(R.id.analysisDateDiffSpinner)).getSelectedItem().toString());


        if (daysApart == 0)
        {
            operationDisplay.append(" from same day");
        } else if (daysApart > 0)
        {
            operationDisplay.append(" from ");
            operationDisplay.append(daysApart.toString());
            operationDisplay.append(" days ago");
        }
        else
        {
            operationDisplay.append(" from ");
            daysApart = Math.abs(daysApart);
            operationDisplay.append(daysApart.toString());
            operationDisplay.append(" days after");
        }

        ((TextView) analysisView.findViewById(R.id.analysisOperationTextView)).setText(operationDisplay.toString());
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

    /**
     * Runs the analysis on the page. And changes the text to Done
     * @param view
     */
    public void onRunButtonClick(View view)
    {
        BiometrixAnalysis analysis = new BiometrixAnalysis();
        String tableName1 = ((Spinner) analysisView.findViewById(R.id.analysisTableSpinner1)).getSelectedItem().toString();
        String tableName2 = ((Spinner) analysisView.findViewById(R.id.analysisTableSpinner2)).getSelectedItem().toString();
        Object spinner1Object = ((Spinner) analysisView.findViewById(R.id.analysisColumnSpinner1)).getSelectedItem();
        Object spinner2Object = ((Spinner) analysisView.findViewById(R.id.analysisColumnSpinner2)).getSelectedItem();

        if (spinner1Object != null && spinner2Object != null) {
            String columnName1 = spinner1Object.toString();
            String columnName2 = spinner2Object.toString();
            Integer daysApart = Integer.parseInt(((Spinner) analysisView.findViewById(R.id.analysisDateDiffSpinner)).getSelectedItem().toString());

            ColumnCorrelation correlation = analysis.getColumnCorrelations(analysisView.getContext(), columnName1, tableName1, columnName2, tableName2, daysApart);

            String numEntriesString = "Number of entries compared: " + Integer.toString(correlation.getCorrelatedEntries());
            ((TextView) analysisView.findViewById(R.id.analysisNumEntriesTextView)).setText(numEntriesString);

            DecimalFormat truncatedValue = new DecimalFormat("#.##");
            String correlationString = "Value of correlation: " + truncatedValue.format(correlation.getCorrelationValue());
            correlationString += " (ranges from -1 to 1, 0 is weak, close to -1 or 1 is strong)";
            ((TextView) analysisView.findViewById(R.id.analysisCorrelationValueTextView)).setText(correlationString);

            String commentString;
            if (correlation.getCorrelatedEntries() == 0) {
                commentString = "There were no matching entries for comparison";
            } else if (correlation.getCorrelatedEntries() < 5) {
                commentString = "There are very few entries compared, the correlation value is likely inaccurate";
            } else if (correlation.getCorrelationValue() == 0) {
                commentString = "It is likely either " + columnName1 + " or " + columnName2 + " contain a lot of zeros. Please try again with more entries";
            } else if (Math.abs(correlation.getCorrelationValue()) < 0.25) {
                commentString = "The correlation between " + columnName1 + " and " + columnName2 + " is very low, it is likely there is no relation between them";
            } else if (Math.abs(correlation.getCorrelationValue()) < 0.5) {
                commentString = "The correlation between " + columnName1 + " and " + columnName2 + " is somewhat low, it is possible there is no relation between them";
            } else if (Math.abs(correlation.getCorrelationValue()) < 0.75) {
                commentString = "The correlation between " + columnName1 + " and " + columnName2 + " is good. Their values may be related";
            } else if (Math.abs(correlation.getCorrelationValue()) < 0.9) {
                commentString = "The correlation between " + columnName1 + " and " + columnName2 + " is very good. Their values are likely related";
            } else if (Math.abs(correlation.getCorrelationValue()) < 1) {
                commentString = "The correlation between " + columnName1 + " and " + columnName2 + " is incredibly strong. They are likely affecting each other, or being caused by the same thing";
            } else if (Math.floor(Math.abs(correlation.getCorrelationValue())) == 1) {
                commentString = "This is a perfect correlation. Make sure you are comparing different things";
            } else {
                commentString = "Something went wrong";
            }


            ((TextView) analysisView.findViewById(R.id.analysisCorrelationCommentsTextView)).setText(commentString);
        }
    }
}
