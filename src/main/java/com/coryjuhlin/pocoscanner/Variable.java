package com.coryjuhlin.pocoscanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cory on 4/13/15.
 */
public class Variable implements IVarPartCollection {
    private String _name;
    private ArrayList<VariablePart> _parts = new ArrayList<>();
    private ArrayList<String> _parameters = new ArrayList<>();

    public String getName() {
        return _name;
    }

    public boolean isVarType() {
        return _parts.size() == 0;
    }

    public boolean AddVariablePart(VariablePart part) {
        return _parts.add(part);
    }

    public boolean AddParameter(String parameter) {
        return _parameters.add(parameter);
    }

    public Variable(String name) {
        this._name = name;
    }

    public String Resolve(VariableBox box, List<String> arguments) {
        StringBuilder builder = new StringBuilder();
        for (VariablePart part : _parts) {
            if (part.IsReference()) {
                // Get what it is referring to
                String dereferenced = DereferenceVariable(box, part, arguments);
                if (dereferenced == null) {
                    return null;
                }
                builder.append(dereferenced);
            } else {
                builder.append(part.GetTextPart());
            }
        }
        return builder.toString();
    }

    String DereferenceVariable(VariableBox box, VariablePart reference, List<String> arguments) {
        // Is it a local variable?
        for (int i = 0; i < _parameters.size(); i++) {
            String paramName = _parameters.get(i);
            if (reference.GetReference().equals(paramName)) {
                return arguments.get(i);
            }
        }

        // Another macro or variable
        Variable var = box.GetVar(reference.GetReference());
        if (var.isVarType()) {
            // Reference to a variable bound at runtime
            return null;
        }

        // Arguments to the variable could be references themselves
        ArrayList<String> dereferencedArguments = new ArrayList<>();
        for (VariablePart part : reference.GetArguments()) {
            if (part.IsReference()) {
                String derefArg = DereferenceVariable(box, part, arguments);
                if (derefArg == null) {
                    return null;
                }
                dereferencedArguments.add(derefArg);
            } else {
                dereferencedArguments.add(part.GetTextPart());
            }
        }

        return var.Resolve(box, dereferencedArguments);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(_name);

        if (isVarType()) {
            builder.append(" - reference");
            return builder.toString();
        }

        builder.append("(");

        for (int i = 0; i < _parameters.size(); i++) {
            builder.append(_parameters.get(i));
            if (i != _parameters.size() - 1) {
                builder.append(", ");
            }
        }


        builder.append(") :: ");

        for (VariablePart part : _parts) {
            builder.append(part.toString());
        }

        return builder.toString();
    }
}
