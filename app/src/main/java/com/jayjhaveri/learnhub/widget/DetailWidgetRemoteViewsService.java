package com.jayjhaveri.learnhub.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by ADMIN-PC on 09-04-2017.
 */

public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new WidgetListRemoteViewFactory(this.getApplicationContext(), intent));
    }
}
