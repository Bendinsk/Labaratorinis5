public class MainActivity extends AppCompatActivity {
    private ListView lvItems;
    private TextView tvStatus;
    private ArrayAdapter listAdapter;
    private Switch swUseAsyncTask;
    private EditText etCurrencyInput;
    private UserChoiceCurrency userChoiceCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.lvItems = findViewById(R.id.lv_items);
        this.tvStatus = findViewById(R.id.tv_status);
        this.swUseAsyncTask = findViewById(R.id.sw_use_async_task);
        this.etCurrencyInput = findViewById(R.id.et_currency_input); // Initialize the EditText

        this.listAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        this.lvItems.setAdapter(this.listAdapter);

        // Set a default currency (e.g., USD)
        this.userChoiceCurrency = new UserChoiceCurrency("USD");
    }

    public void onBtnGetDataClick(View view) {
        String userEnteredCurrency = etCurrencyInput.getText().toString().trim();

        if (!userEnteredCurrency.isEmpty()) {
            // Update the selected currency based on user input
            userChoiceCurrency.setSelectedCurrency(userEnteredCurrency);
        }

        this.tvStatus.setText(R.string.loading_data);
        if (this.swUseAsyncTask.isChecked()) {
            getDataByAsyncTask(userChoiceCurrency);
            Toast.makeText(this, R.string.msg_using_async_task, Toast.LENGTH_LONG).show();
        } else {
            getDataByThread(userChoiceCurrency);
            Toast.makeText(this, R.string.msg_using_thread, Toast.LENGTH_LONG).show();
        }
    }

    public void getDataByAsyncTask(UserChoiceCurrency userChoiceCurrency) {
        new AsyncDataLoader() {
            @Override
            public void onPostExecute(String result) {
                tvStatus.setText(getString(R.string.data_loaded) + result);
            }
        }.execute(Constants.FLOATRATES_API_URL, userChoiceCurrency.getSelectedCurrency());
    }

    public void getDataByThread(UserChoiceCurrency userChoiceCurrency) {
        this.tvStatus.setText(R.string.loading_data);
        Runnable getDataAndDisplayRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final String result = ApiDataReader.getValuesFromApi(Constants.FLOATRATES_API_URL, userChoiceCurrency.getSelectedCurrency());
                    Runnable updateUIRunnable = new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText(getString(R.string.data_loaded) + result);
                        }
                    };
                    runOnUiThread(updateUIRunnable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(getDataAndDisplayRunnable);
        thread.start();
    }
}
