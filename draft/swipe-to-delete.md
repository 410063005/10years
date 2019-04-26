
使用 ItemTouchHelper.SimpleCallback 可以很方便地为 RecyclerView 添加以下功能：

+ swipe items
+ move items

---

原文 https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28

# 使用 ItemTouchHelper 侧滑删除
现在来为静态 view 添加一些操作，为此需要实现 `ItemTouchHelper.Callback` 接口中的3个方法：

```java
public abstract int getMovementFlags(@NonNull RecyclerView var1, @NonNull ViewHolder var2);
public abstract boolean onMove(@NonNull RecyclerView var1, @NonNull ViewHolder var2, @NonNull ViewHolder var3);
public abstract int getMovementFlags(@NonNull RecyclerView var1, @NonNull ViewHolder var2);
```

+ `getMovementFlags()` 方法告诉 `ItemTouchHelper` RecyclerView 需要响应哪些操作
+ `onMove, onSwiped` 方法用于描述如何响应指定的方法

```java
class SwipeController extends Callback {

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, LEFT | RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }
}
```

在 `MainActivity` 中使用 `attachToRecyclerView()` 方法将 helper 和 RecyclerView 关联起来：

```java
// MainActivity.java
SwipeController swipeController = new SwipeController();
ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
itemTouchhelper.attachToRecyclerView(recyclerView);
```

现在可以对 RecyclerView 中的 item 进行侧滑了。但你会注意到，我们会将 item 滑出屏幕以外。需要有办法阻止 item 被滑出屏幕外。解决办法是覆盖 `ItemTouchHelper.Callback` 接口中的 `convertToAbsoluteDirection()` 方法。

```java
// SwipeController.java
@Override
public int convertToAbsoluteDirection(int flags, int layoutDirection) {
    if (swipeBack) {
        swipeBack = false;
        return 0;
    }
    return super.convertToAbsoluteDirection(flags, layoutDirection);
}
```

好吧，你会问 `swipeBack` 从哪里来的。我们只需要为 RecyclerView 设置 `onTouchListener`。在检查到 swipe 操作后我们将 `swipeBack` 设置为 true。

```java
// SwipeController.java
@Override
public void onChildDraw(Canvas c, 
    RecyclerView recyclerView, 
    RecyclerView.ViewHolder viewHolder, 
    float dX, float dY, 
    int actionState, boolean isCurrentlyActive) {
    
    if (actionState == ACTION_STATE_SWIPE) {
        setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
}

private void setTouchListener(Canvas c, 
    RecyclerView recyclerView, 
    RecyclerView.ViewHolder viewHolder, 
    float dX, float dY, 
    int actionState, boolean isCurrentlyActive) {
    
    recyclerView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
            return false;
        }
    });
}
```

开始绘制按钮之前需要为它们创建一些展示逻辑。老实说，这是最重要的一部分，也最难解释清楚，所以单独分一节来说。

# 带按钮状态的 Swipe Controller

首先，先创建 `buttonShowedState` 属性来保存当前展示的按钮的状态信息。

```java
// SwipeController.java
enum ButtonsState {
    GONE,
    LEFT_VISIBLE,
    RIGHT_VISIBLE
}
class SwipeController extends Callback {
    private boolean swipeBack = false;
    private ButtonsState buttonShowedState = ButtonsState.GONE;
    private static final float buttonWidth = 300;
    ...
}
```

需要一些条件来正确设置状态。在 `OnTouchListener` 中检查用户将 item 向左或向右滑动了多少。如果滑动距离足够的话，就修改按钮状态：

```java
// SwipeController.java
private void setTouchListener(final Canvas c, 
    final RecyclerView recyclerView, 
    final RecyclerView.ViewHolder viewHolder, 
    final float dX, final float dY, 
    final int actionState, final boolean isCurrentlyActive) {
    recyclerView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
            if (swipeBack) {
                if (dX < -buttonWidth) buttonShowedState = ButtonsState.RIGHT_VISIBLE;
                else if (dX > buttonWidth) buttonShowedState  = ButtonsState.LEFT_VISIBLE;

                if (buttonShowedState != ButtonsState.GONE) {
    setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    setItemsClickable(recyclerView, false);
}
            }
            return false;
        }
    });
}

```

如果 `buttonShowedState` 不是 GONE 状态的话，需要覆盖 touch listener 并且模拟 RecyclerView 的点击事件。为什么说是要模拟呢，因为 item 上已经有 OnClickListener 了，所以要将原有的 OnClickListener 禁用以防止冲突。

```java
// SwipeController.java
private void setTouchDownListener(final Canvas c, 
    final RecyclerView recyclerView, 
    final RecyclerView.ViewHolder viewHolder, 
    final float dX, final float dY, 
    final int actionState, final boolean isCurrentlyActive) {
    recyclerView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
            return false;
        }
    });
}

private void setTouchUpListener(final Canvas c, 
    final RecyclerView recyclerView, 
    final RecyclerView.ViewHolder viewHolder, 
    final float dX, final float dY, 
    final int actionState, final boolean isCurrentlyActive) {
    recyclerView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                SwipeController.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);
                recyclerView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
                setItemsClickable(recyclerView, true);
                swipeBack = false;
                buttonShowedState = ButtonsState.GONE;
            }
            return false;
        }
    });
}

private void setItemsClickable(RecyclerView recyclerView, 
boolean isClickable) {
    for (int i = 0; i < recyclerView.getChildCount(); ++i) {
        recyclerView.getChildAt(i).setClickable(isClickable);
    }
}
```

当你点击 RecyclerView 时，重置 SwipeController 的状态并重绘。

# 绘制按钮
`onChildDraw()` 方法可以访问 cavas 参数。在这个方法中要给 `buttonInstance` 赋值以方便后续使用。

```java
// SwipeController.java
@Override
public void onChildDraw(Canvas c, 
    RecyclerView recyclerView, 
    RecyclerView.ViewHolder viewHolder, 
    float dX, float dY, 
    int actionState, boolean isCurrentlyActive) {
    // ...
    drawButtons(c, viewHolder);
}
private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder) {
    float buttonWidthWithoutPadding = buttonWidth - 20;
    float corners = 16;

    View itemView = viewHolder.itemView;
    Paint p = new Paint();

    RectF leftButton = new RectF(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + buttonWidthWithoutPadding, itemView.getBottom());
    p.setColor(Color.BLUE);
    c.drawRoundRect(leftButton, corners, corners, p);
    drawText("EDIT", c, leftButton, p);

    RectF rightButton = new RectF(itemView.getRight() - buttonWidthWithoutPadding, itemView.getTop(), itemView.getRight(), itemView.getBottom());
    p.setColor(Color.RED);
    c.drawRoundRect(rightButton, corners, corners, p);
    drawText("DELETE", c, rightButton, p);

    buttonInstance = null;
    if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
        buttonInstance = leftButton;
    }
    else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
        buttonInstance = rightButton;
    }
}

private void drawText(String text, Canvas c, RectF button, Paint p) {
    float textSize = 60;
    p.setColor(Color.WHITE);
    p.setAntiAlias(true);
    p.setTextSize(textSize);

    float textWidth = p.measureText(text);
    c.drawText(text, button.centerX()-(textWidth/2), button.centerY()+(textSize/2), p);
}
```

正如你看到的那样，上图中的 demo 基本可以工作。但当你滚动 RecyclerView 时按钮会消失。这是因为 `onChildDraw()` 方法仅在 swiping 或 moving items (而不是 scrolling)时才会被触发。为了避免这个问题我们使用 `ItemDecoration` 来保证按钮被重新绘制。

```java
// MainActivity.java
private void setupRecyclerView() {
    // ...
    recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            swipeController.onDraw(c);
        }
    });
}
```

SwipeController 实现 `onDraw()` 方法。但我们并不直接调用这里的 `onDraw` 方法。而是在 `onChildDraw()` 方法中给 currentItemViewHolder 属性赋值。

```java
// SwipeController.java
private RecyclerView.ViewHolder currentItemViewHolder = null;
if (buttonShowedState == ButtonsState.GONE) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
    currentItemViewHolder = viewHolder;
}// PlayersSwipeController.java
private RecyclerView.ViewHolder currentItemViewHolder = null;
```

# 为按钮添加响应
至此我们基本就完成了所有工作，除了 button 不能响应用户点击。创建抽象类 `SwipeControllerActions`，它有两个方法：

```java
// SwipeControllerActions.java
package pl.fanfatal.swipecontrollerdemo;

public abstract class SwipeControllerActions {

    public void onLeftClicked(int position) {}

    public void onRightClicked(int position) {}
}
```

将 action 传给 SwipeController：

```java
// SwipeController.java
public SwipeController(SwipeControllerActions buttonsActions) {
    this.buttonsActions = buttonsActions;
}
```

在 `touchUpListener` 中检查按钮是否被点击：

```java
// SwipeController.java
if (buttonsActions != null && buttonInstance != null && buttonInstance.contains(event.getX(), event.getY())) {
    if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
        buttonsActions.onLeftClicked(viewHolder.getAdapterPosition());
    }
    else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
        buttonsActions.onRightClicked(viewHolder.getAdapterPosition());
    }
}
```

在 `MainActivity` 中传入回调：

```
// MainActivity.java
swipeController = new SwipeController(new SwipeControllerActions() {
    @Override
    public void onRightClicked(int position) {
        mAdapter.players.remove(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
    }
});
```

最终的完整代码见 [github](https://github.com/FanFataL/swipe-controller-demo)。

# 参考

[](https://medium.com/@kitek/recyclerview-swipe-to-delete-easier-than-you-thought-cff67ff5e5f6)

[](https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28)