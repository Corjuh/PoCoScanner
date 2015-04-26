package com.coryjuhlin.pocoscanner;

import com.coryjuhlin.pocoscanner.antlr.PoCoParser;
import com.coryjuhlin.pocoscanner.antlr.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Stack;


public class RegexVisitor extends PoCoParserBaseVisitor<String> {
    private Stack<StringBuilder> regexStack;
    public ArrayList<String> matchStrings;
    private VariableBox variableBox;
    private Stack<VariablePart> varRefStack = new Stack<>();
    private boolean runtimeReference = false;

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
        //String text = visit(ctx.re(0));
        visit(ctx.re(0));

        StringBuilder builder = regexStack.pop();
        if (!runtimeReference) {
            matchStrings.add(builder.toString());
        }

        runtimeReference = false;
        return null;
    }

    VariablePart parseVarRef(@NotNull PoCoParser.ReContext ctx) {
        varRefStack.push(new VariablePart(ctx.qid().getText(), true));
        if (ctx.opparamlist() != null) {
            visitOpparamlist(ctx.opparamlist());
        }
        VariablePart toReturn = varRefStack.pop();
        return toReturn;
    }

    @Override
    public String visitRe(@NotNull PoCoParser.ReContext ctx) {
        if (regexStack.size() == 0 || runtimeReference) {
            return null;
        }

        StringBuilder localBuilder = new StringBuilder();

        if (ctx.DOLLAR() != null) {
            VariablePart ref = parseVarRef(ctx);
            Variable var = variableBox.GetVar(ref.GetReference());

            if (var == null || var.isVarType()) {
                runtimeReference = true;
            } else {
                localBuilder.append(var.Resolve(variableBox, ref.GetArguments()));
            }
        }

        else if (ctx.AT() != null) {
            // TODO: Do something about the variable binding
        }

        else {
            /* Iterate over all children. Check and see if their visit
             * method was customized to return a string. Otherwise, get
             * the raw text.
             */
            for (ParseTree tree : ctx.children) {
                if (tree instanceof PoCoParser.ReContext) {
                    visit(tree);
                } else {
                    localBuilder.append(tree.getText());
                }
            }
        }

        if (varRefStack.size() > 0) {
            varRefStack.peek().AddVariablePart(new VariablePart(localBuilder.toString(), false));
        } else {
            regexStack.peek().append(localBuilder.toString());
        }

        return null;
    }
}
