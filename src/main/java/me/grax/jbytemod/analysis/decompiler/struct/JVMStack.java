package me.grax.jbytemod.analysis.decompiler.struct;

import java.lang.reflect.Field;
import java.util.EmptyStackException;
import java.util.Stack;

import me.grax.jbytemod.analysis.decompiler.code.ast.Expression;
import me.grax.jbytemod.analysis.decompiler.code.ast.VarType;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.DebugStackExpression;
import me.grax.jbytemod.analysis.decompiler.code.ast.expressions.DebugStackUnknownExpression;
import me.grax.jbytemod.analysis.decompiler.struct.exception.StackException;

public class JVMStack {

  private Stack<Expression> list;

  public JVMStack() {
    this.list = new Stack<Expression>();
  }

  private int belowStackCount = 0;

  public JVMStack(JVMStack preStack) {
    this.list = new Stack<Expression>();
    for (Expression e : preStack.list) {
      list.add(new DebugStackExpression(belowStackCount++, e.size(), getVarType(e)));
    }
  }

  private VarType getVarType(Expression array) {
    for (Field f : array.getClass().getDeclaredFields()) {
      if (f.getType() == VarType.class) {
        f.setAccessible(true);
        try {
          return (VarType) f.get(array);
        } catch (Exception e) {
        }
      }
    }
    return null;
  }

  public void push(Expression o, boolean twoword) {
    if (twoword) {
      if (o.size() != 2) {
        throw new StackException("not a wide value: " + o.size());
      }
    } else {
      if (o.size() != 1) {
        throw new StackException("not a 1-size: " + o.size());
      }
    }
    push(o);
  }

  public void push(Expression o) {
    list.push(o);
  }

  public Expression pop() {
    Expression top = peek();
    if (top.size() != 1) {
      throw new StackException("Top is " + top.size() + "-word value, cannot pop");
    }
    return list.pop();
  }

  public Expression peek() {
    try {
      return list.peek();
    } catch (EmptyStackException e) {
      //stack empty, maybe a try catch block?
      list.push(new DebugStackUnknownExpression(belowStackCount, 1, null));
      return list.peek();
    }
  }

  public void push(Expression o, int i, boolean twoword) {
    if (twoword) {
      if (o.size() != 2) {
        throw new StackException("not a wide value: " + o.size());
      }
    } else {
      if (o.size() != 1) {
        throw new StackException("not a 1-size: " + o.size());
      }
    }
    push(o, i);
  }

  public void push(Expression o, int i) {
    list.add(list.size() - 1 - i, o);
  }

  public int size() {
    return list.size();
  }

  public Expression pop2() {
    Expression top = peek();
    int size = top.size();
    if (size == 2) {
      return list.pop();
    } else if (size == 1) {
      Expression oldTop = list.pop();
      if (peek().size() != 1) {
        throw new StackException("Second value is " + top.size() + "-word, cannot pop2 (Top was 1-word: " + oldTop.getClass().getSimpleName() + " "
            + oldTop.toString() + ")");
      }
      return list.pop();
    }
    throw new StackException(String.valueOf(size));
  }

  public Expression peek2() {
    return list.elementAt(list.size() - 2);
  }

  public Stack<Expression> getList() {
    return list;
  }

}
