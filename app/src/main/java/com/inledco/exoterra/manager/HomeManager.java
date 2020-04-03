package com.inledco.exoterra.manager;

public class HomeManager {
//    private final String TAG = "HomeManager";
//
//    private final Map<String, Home> mHomeMap;
//    private final List<Home> mHomeList;
//
//    private AsyncTask<Void, Void, Void> mAsyncTask;
//
//    private HomeManager() {
//        mHomeMap = new ConcurrentHashMap<>();
//        mHomeList = new ArrayList<>();
//    }
//
//    public static HomeManager getInstance() {
//        return LazyHolder.INSTANCE;
//    }
//
//    public boolean contains(final String homeid) {
//        return mHomeMap.containsKey(homeid);
//    }
//
//    public void clear() {
//        mHomeMap.clear();
//        mHomeList.clear();
//    }
//
//    public void remove(final String homeid) {
//        for (int i = 0; i < mHomeList.size(); i++) {
//            if (TextUtils.equals(homeid, mHomeList.get(i).getHome().id)) {
//                mHomeList.remove(i);
//                return;
//            }
//        }
//        mHomeMap.remove(homeid);
//    }
//
//    private void updateHomes(@NonNull final List<HomeApi.HomesResponse.Home> homes) {
//        final Set<String> oldsets = new HashSet<>(mHomeMap.keySet());
//        final Set<String> newsets = new HashSet<>();
//        for (HomeApi.HomesResponse.Home home : homes) {
//            newsets.add(home.id);
//        }
//        for (String key : oldsets) {
//            if (!newsets.contains(key)) {
//                mHomeMap.remove(key);
//            }
//        }
//        for (HomeApi.HomesResponse.Home home : homes) {
//            final String homeid = home.id;
//            if (mHomeMap.containsKey(homeid)) {
//                mHomeMap.get(homeid).setHome(home);
//            } else {
//                mHomeMap.put(homeid, new Home(home));
//            }
//        }
//        mHomeList.clear();
//        mHomeList.addAll(mHomeMap.values());
//    }
//
//    public Home getHome(final String homeid) {
//        if (!TextUtils.isEmpty(homeid) && mHomeMap.containsKey(homeid)) {
//            return mHomeMap.get(homeid);
//        }
//        return null;
//    }
//
//    public Home getDeviceHome(final String pid, final String mac) {
//        for (Home home : mHomeList) {
//            for (HomeApi.HomeDevicesResponse.Device dev : home.getDevices()) {
//                if (TextUtils.equals(pid, dev.productId) && TextUtils.equals(mac, dev.mac)) {
//                    return home;
//                }
//            }
//        }
//        return null;
//    }
//
//    public Home getDeviceHome(final Device device) {
//        if (device != null) {
//            final String pid = device.getXDevice().getProductId();
//            final String mac = device.getXDevice().getMacAddress();
//            return getDeviceHome(pid, mac);
//        }
//        return null;
//    }
//
//    public List<Home> getHomeList() {
//        return mHomeList;
//    }
//
//    public void refreshHomeList(final XlinkRequestCallback<List<Home>> callback) {
//        if (mAsyncTask != null) {
//            mAsyncTask.cancel(true);
//        }
//        XlinkCloudManager.getInstance().getHomeList(new XlinkRequestCallback<HomeApi.HomesResponse>() {
//            @Override
//            public void onError(String error) {
//                if (callback != null) {
//                    callback.onError(error);
//                }
//            }
//
//            @Override
//            public void onSuccess(HomeApi.HomesResponse response) {
////                mHomeMap.clear();
////                mHomeList.clear();
////                for (HomeApi.HomesResponse.Home home : response.list) {
////                    mHomeMap.put(home.id, new Home(home));
////                }
////                mHomeList.addAll(mHomeMap.values());
//
//                updateHomes(response.list);
//                EventBus.getDefault().post(new HomesRefreshedEvent());
//                if (callback != null) {
//                    callback.onSuccess(mHomeList);
//                }
//                getAllHomePropertiesAndDevices();
//            }
//        });
//    }
//
//    public void getAllHomePropertiesAndDevices() {
//        if (mHomeList.size() == 0) {
//            return;
//        }
//        if (mAsyncTask != null) {
//            mAsyncTask.cancel(true);
//        }
//        mAsyncTask = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
//                for (Home home : mHomeList) {
//                    String homeid = home.getHome().id;
//                    if (!home.isPropertySynchronized()) {
//                        XlinkResult<HomeProperty> result1 = XlinkCloudManager.getInstance().getHomeProperty(homeid);
//                        if (result1.isSuccess()) {
//                            home.setProperty(result1.getResult());
//                            EventBus.getDefault().post(new HomePropertyChangedEvent(homeid));
//                        }
//                    }
//                    if (!home.isDevicesSynchronized()) {
//                        XlinkResult<HomeApi.HomeDevicesResponse> result2 = XlinkCloudManager.getInstance().getHomeDeviceList(homeid);
//                        if (result2.isSuccess()) {
//                            home.setDevices(result2.getResult().list);
//                            EventBus.getDefault().post(new HomeDeviceChangedEvent(homeid));
//                        }
//                    }
//                }
//                return null;
//            }
//        };
//        mAsyncTask.execute();
//    }
//
//    public void refreshHome(@NonNull final Home home) {
//        final String homeid = home.getHome().id;
//        XlinkCloudManager.getInstance().getHomeProperty(homeid, new XlinkRequestCallback<HomeProperty>() {
//            @Override
//            public void onError(String error) {
//
//            }
//
//            @Override
//            public void onSuccess(HomeProperty property) {
//                home.setProperty(property);
//                EventBus.getDefault().post(new HomePropertyChangedEvent(homeid));
//                XlinkCloudManager.getInstance().getHomeDeviceList(homeid, new XlinkRequestCallback<HomeApi.HomeDevicesResponse>() {
//                    @Override
//                    public void onError(String error) {
//
//                    }
//
//                    @Override
//                    public void onSuccess(HomeApi.HomeDevicesResponse response) {
//                        home.setDevices(response.list);
//                        EventBus.getDefault().post(new HomeDeviceChangedEvent(homeid));
//                    }
//                });
//            }
//        });
//    }
//
//    public void refreshHomeDevices(final Home home) {
//        if (home == null) {
//            return;
//        }
//        final String homeid = home.getHome().id;
//        XlinkCloudManager.getInstance().getHomeDeviceList(homeid, new XlinkRequestCallback<HomeApi.HomeDevicesResponse>() {
//            @Override
//            public void onError(String error) {
//
//            }
//
//            @Override
//            public void onSuccess(HomeApi.HomeDevicesResponse response) {
//                Log.e(TAG, "onSuccess: " + response.count);
//                home.setDevices(response.list);
//                EventBus.getDefault().post(new HomeDeviceChangedEvent(homeid));
//            }
//        });
//    }
//
//    public void refreshHomeDevices(final String homeid) {
//        final Home home = getHome(homeid);
//        refreshHomeDevices(home);
//    }
//
//    private static class LazyHolder {
//        private static final HomeManager INSTANCE = new HomeManager();
//    }
}
