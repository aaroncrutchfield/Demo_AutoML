package com.example.demos.demo_automl;

import com.google.api.services.tasks.model.Task;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Asynchronously load the tasks.
 *
 * @author Yaniv Inbar
 */
class AsyncLoadTasks extends CommonAsyncTask {


    AsyncLoadTasks(MainActivity mainActivity) {
        super(mainActivity);
    }


    @Override
    protected void doInBackground() throws IOException {
        List<String> result = new ArrayList<String>();
        List<Task> tasks =
                client.tasks().list("@default").setFields("items/title").execute().getItems();
        if (tasks != null) {
            for (Task task : tasks) {
                result.add(task.getTitle());
            }
        } else {
            result.add("No tasks.");
        }
        activity.tasksList = result;
    }


    static void run(MainActivity mainActivity) {
        new AsyncLoadTasks(mainActivity).execute();
    }
}
