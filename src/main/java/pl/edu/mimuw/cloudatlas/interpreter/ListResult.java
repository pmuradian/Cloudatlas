//package pl.edu.mimuw.cloudatlas.interpreter;
//
//import pl.edu.mimuw.cloudatlas.model.Type;
//import pl.edu.mimuw.cloudatlas.model.Value;
//import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
//import pl.edu.mimuw.cloudatlas.model.ValueList;
//
//public class ListResult extends Result {
//
//    @Override
//    protected ResultSingle binaryOperationTyped(BinaryOperation operation, ResultSingle right) {
//        return new ResultSingle(operation.perform(value, right.value));
//    }
//
//    @Override
//    public ResultSingle unaryOperation(UnaryOperation operation) {
//        return new ResultSingle(operation.perform(value));
//    }
//
//    @Override
//    protected Result callMe(BinaryOperation operation, Result left) {
//        return this;
////        return left.binaryOperationTyped(operation, this);
//    }
//
//    @Override
//    public ValueList getValue() {
//        return value;
//    }
//
//    @Override
//    public ValueList getList() {
//        ValueList list = new ValueList(value.getType());
//        list.add(value);
//        return list;
//    }
//
//    @Override
//    public ValueList getColumn() {
//        throw new UnsupportedOperationException("Not a ResultColumn.");
//    }
//
//    @Override
//    public Result filterNulls() {
//        throw new UnsupportedOperationException("Operation filterNulls not supported on ResultSingle.");
//    }
//
//    @Override
//    public Result first(int size) {
//        throw new UnsupportedOperationException("Operation first not supported on ResultSingle.");
//    }
//
//    @Override
//    public Result last(int size) {
//        throw new UnsupportedOperationException("Operation last not supported on ResultSingle.");
//    }
//
//    @Override
//    public Result random(int size) {
//        throw new UnsupportedOperationException("Operation random not supported on ResultSingle.");
//    }
//
//    @Override
//    public ResultSingle convertTo(Type to) {
//        return new ResultSingle(value.convertTo(to));
//    }
//
//    @Override
//    public ResultSingle isNull() {
//        return new ResultSingle(new ValueBoolean(value.isNull()));
//    }
//
//    @Override
//    public Type getType() {
//        return value.getType();
//    }
//}
