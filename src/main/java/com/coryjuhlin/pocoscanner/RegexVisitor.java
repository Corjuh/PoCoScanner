package com.coryjuhlin.pocoscanner;

import com.coryjuhlin.pocoscanner.antlr.PoCoParser;
import com.coryjuhlin.pocoscanner.antlr.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by cory on 4/13/15.
 */
public class RegexVisitor extends PoCoParserBaseVisitor<String> {
    Stack<StringBuilder> regexStack;
    public ArrayList<String> matchStrings;
    private VariableBox variableBox;

    public RegexVisitor(VariableBox variableBox) {
        regexStack = new Stack<>();
        matchStrings = new ArrayList<>();
        this.variableBox = variableBox;
    }

    @Override
    public String visitExch(@NotNull PoCoParser.ExchContext ctx) {
        if (ctx.INPUTWILD() != null) {
            matchStrings.add(".*");
        }

        return super.visitExch(ctx);
    }

    @Override
    public String visitIre(@NotNull PoCoParser.IreContext ctx) {
        regexStack.push(new StringBuilder());

        // Only visit first RE (second one would be the value matcher of RESULT IREs)
        String text = visit(ctx.re(0));

        regexStack.pop();
        matchStrings.add(text);
        return null;
    }

    @Override
    public String visitRe(@NotNull PoCoParser.ReContext ctx) {
        if (regexStack.size() == 0) {
            return null;
        }

        if (ctx.DOLLAR() != null) {
            // TODO: Do something about the variable reference
        }

        if (ctx.AT() != null) {
            // TODO: Do something about the variable binding
        }

        /* Iterate over all children. Check and see if their visit
         * method was customized to return a string. Otherwise, get
         * the raw text.
         */
        StringBuilder builder = new StringBuilder();
        for (ParseTree tree : ctx.children) {
            String visitResult = visit(tree);
            if (visitResult != null && visitResult.length() > 0) {
                builder.append(visitResult);
            } else {
                builder.append(tree.getText());
            }
        }
        return builder.toString();
    }
}
