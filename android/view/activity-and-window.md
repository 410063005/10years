# Activity 与 Window 的关系

```java
public abstract class Window {
    private Callback mCallback;

    /**
     * API from a Window back to its caller.  This allows the client to
     * intercept key dispatching, panels and menus, etc.
     */
    public interface Callback {    
        /**
         * Called to process touch screen events.  At the very least your
         * implementation must call
         * {@link android.view.Window#superDispatchTouchEvent} to do the
         * standard touch screen processing.
         *
         * @param event The touch screen event.
         *
         * @return boolean Return true if this event was consumed.
         */
        public boolean dispatchTouchEvent(MotionEvent event);
    }    
}

public class Activity implements Window.Callback {
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            onUserInteraction();
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }
}
```