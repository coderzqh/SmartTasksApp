package com.example.smarttasksapp.core.lifecycle;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fragment生命周期管理器
 * 管理Fragment级别的生命周期和资源清理
 */
public class FragmentLifecycleManager {
    private static final String TAG = "FragmentLifecycleManager";
    private static FragmentLifecycleManager instance;
    
    private final Map<Fragment, String> fragmentScopes;
    private final AppLifecycleManager lifecycleManager;
    
    private FragmentLifecycleManager(AppLifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
        this.fragmentScopes = new ConcurrentHashMap<>();
    }
    
    public static synchronized FragmentLifecycleManager getInstance(AppLifecycleManager lifecycleManager) {
        if (instance == null) {
            instance = new FragmentLifecycleManager(lifecycleManager);
        }
        return instance;
    }
    
    /**
     * 注册Fragment生命周期回调
     */
    public void registerFragmentLifecycle(FragmentManager fragmentManager) {
        fragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
                String scopeName = "Fragment_" + f.getClass().getSimpleName() + "_" + f.hashCode();
                fragmentScopes.put(f, scopeName);
                Log.d(TAG, "Fragment created: " + f.getClass().getSimpleName() + " with scope: " + scopeName);
            }
            
            @Override
            public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
                String scopeName = fragmentScopes.remove(f);
                if (scopeName != null) {
                    // 销毁Fragment相关的Scope
                    lifecycleManager.destroyScope(scopeName);
                    Log.d(TAG, "Fragment destroyed: " + f.getClass().getSimpleName() + " with scope: " + scopeName);
                } else {
                    Log.w(TAG, "Fragment destroyed but no scope found: " + f.getClass().getSimpleName());
                }
            }
        }, true);
    }
    
    /**
     * 获取Fragment的作用域名称
     */
    public String getFragmentScopeName(Fragment fragment) {
        return fragmentScopes.get(fragment);
    }
    
    /**
     * 检查Fragment是否有作用域
     */
    public boolean hasFragmentScope(Fragment fragment) {
        return fragmentScopes.containsKey(fragment);
    }
    
    /**
     * 获取当前活动的Fragment数量
     */
    public int getActiveFragmentCount() {
        return fragmentScopes.size();
    }
    
    /**
     * 手动销毁Fragment作用域（用于特殊情况）
     */
    public void destroyFragmentScope(Fragment fragment) {
        String scopeName = fragmentScopes.remove(fragment);
        if (scopeName != null) {
            lifecycleManager.destroyScope(scopeName);
            Log.d(TAG, "Manually destroyed fragment scope: " + scopeName);
        }
    }
}
