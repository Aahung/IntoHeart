package ee3316.intoheart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ee3316.intoheart.Data.UserStore;
import ee3316.intoheart.HTTP.Connector;
import ee3316.intoheart.HTTP.JCallback;
import ee3316.intoheart.HTTP.Outcome;
import ee3316.intoheart.UIComponent.SimpleAlertController;

/**
 * Created by aahung on 3/7/15.
 */
public class UserinfoFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    UserStore userStore;
    Connector connector;

    public static UserinfoFragment newInstance(int sectionNumber) {
        UserinfoFragment fragment = new UserinfoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public UserinfoFragment() {

    }

    @InjectView(R.id.login_block)
    LinearLayout login_block;
    @InjectView(R.id.logout_block)
    LinearLayout logout_block;

    private void setVisibility() {
        if (userStore.getLogin()) {
            login_block.setVisibility(View.GONE);
            logout_block.setVisibility(View.VISIBLE);
        } else {
            login_block.setVisibility(View.VISIBLE);
            logout_block.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_userinfo, container, false);
        setHasOptionsMenu(true);
        ButterKnife.inject(this, rootView);
        {
            userStore = new UserStore(getActivity());
            setVisibility();
            nameEdit.setText(userStore.name);
            nameEdit.addTextChangedListener(onchangeListener);
            if (userStore.getAge() != null)
                ageEdit.setText(userStore.getAge().toString());
            ageEdit.addTextChangedListener(onchangeListener);
            if (userStore.getHeight() != null)
                heightEdit.setText(userStore.getHeight().toString());
            heightEdit.addTextChangedListener(onchangeListener);
            if (userStore.getWeight() != null)
                weightEdit.setText(userStore.getWeight().toString());
            weightEdit.addTextChangedListener(onchangeListener);
            emergencyEdit.setText(userStore.emergencyTel);
            emergencyEdit.addTextChangedListener(onchangeListener);
        }
        connector = new Connector();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    public TextWatcher onchangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            userStore.name = nameEdit.getText().toString().trim();
            userStore.emergencyTel = emergencyEdit.getText().toString().trim();
            try {
                userStore.age = Integer.valueOf(ageEdit.getText().toString());
            } catch (Exception ex) {}
            try {
                userStore.height = Integer.valueOf(heightEdit.getText().toString());
            } catch (Exception ex) {}
            try {
                userStore.weight = Integer.valueOf(weightEdit.getText().toString());
            } catch (Exception ex) {}
            userStore.save();
        }
    };

    @InjectView(R.id.name_edit) EditText nameEdit;
    @InjectView(R.id.age_edit) EditText ageEdit;
    @InjectView(R.id.height_edit) EditText heightEdit;
    @InjectView(R.id.weight_edit) EditText weightEdit;
    @InjectView(R.id.emergency_edit) EditText emergencyEdit;

    @OnClick(R.id.login_button)
    public void loginPrompt(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alertdialog_login, null);
        final EditText emailEdit = ((EditText)dialogView.findViewById(R.id.email_edit));
        final EditText passwordEdit = ((EditText)dialogView.findViewById(R.id.password_edit));
        emailEdit.setText(tmpEmail);
        passwordEdit.setText(tmpPassword);
        builder.setView(dialogView)
                .setTitle("Log in")
                .setNeutralButton("Cancel", null)
                .setPositiveButton("Log in", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = emailEdit
                                .getText().toString();
                        String password = passwordEdit
                                .getText().toString();
                        dialog.dismiss();
                        login(email, password);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    String tmpEmail, tmpPassword;

    private void login(final String email, final String password) {
        tmpEmail = email;
        tmpPassword = password;
        connector.login(email, password, new JCallback<Outcome>() {
            @Override
            public void call(Outcome outcome) {
                if (outcome.success) {
                    nameEdit.setText(outcome.getString());
                    userStore.name = outcome.getString();
                    userStore.email = email;
                    userStore.password = password;
                    userStore.save();
                    SimpleAlertController.showSimpleMessage("Cool",
                            String.format("Welcome back, %s", outcome.getString()), getActivity());
                    setVisibility();
                } else {
                    SimpleAlertController.showSimpleMessageWithHandler("Sorry",
                            outcome.getString(), getActivity(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loginPrompt(null);
                        }
                    });
                }
            }
        });
    }

    @OnClick(R.id.register_button)
    public void registerPrompt(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alertdialog_signup, null);
        final EditText emailEdit = ((EditText)dialogView.findViewById(R.id.email_edit));
        final EditText nameEdit = ((EditText)dialogView.findViewById(R.id.name_edit));
        nameEdit.setText(this.nameEdit.getText().toString());
        final EditText passwordEdit = ((EditText)dialogView.findViewById(R.id.password_edit));
        builder.setView(dialogView)
                .setTitle("Register")
                .setNeutralButton("Cancel", null)
                .setPositiveButton("Register", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = nameEdit.getText().toString();
                        String email = emailEdit
                                .getText().toString();
                        String password = passwordEdit
                                .getText().toString();
                        dialog.dismiss();
                        register(name, email, password);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void register(final String name, final String email, final String password) {
        connector.register(name, email, password, new JCallback<Outcome>() {
            @Override
            public void call(Outcome outcome) {
                if (outcome.success) {
                    nameEdit.setText(name);
                    userStore.name = name;
                    userStore.email = email;
                    userStore.password = password;
                    userStore.save();
                    SimpleAlertController.showSimpleMessage("Cool",
                            String.format("Welcome %s", name), getActivity());
                    setVisibility();
                } else {
                    SimpleAlertController.showSimpleMessage("Cool",
                            "Failed to register", getActivity());
                }
            }
        });
    }

    @OnClick(R.id.logout_button)
    public void logout() {
        tmpEmail = null;
        tmpPassword = null;
        userStore.email = null;
        userStore.password = null;
        userStore.save();
        setVisibility();
    }
}
