package styshak.com.taskslist.objects;

import java.util.Comparator;

public class TodoListComparator {

    private static DateComparator dateComparator;
    private static NameComparator nameComparator;
    private static PriorityComparator priorityComparator;

    public static Comparator<TodoDocument> getDateComparator(){
        if (dateComparator == null){
            dateComparator = new DateComparator();
        }
        return dateComparator;
    }

    public static Comparator<TodoDocument> getNameComparator(){
        if (nameComparator == null){
            nameComparator = new NameComparator();
        }
        return nameComparator;
    }

    public static Comparator<TodoDocument> getPriorityComparator(){
        if (priorityComparator == null){
            priorityComparator = new PriorityComparator();
        }
        return priorityComparator;
    }

    private static class PriorityComparator implements Comparator<TodoDocument> {

        @Override
        public int compare(TodoDocument todo1, TodoDocument todo2) {
            int result = todo2.getPriorityType().compareTo(todo1.getPriorityType());
            if (result == 0){
                result = todo2.getCreateDate().compareTo(todo1.getCreateDate());
            }
            return result;
        }
    }

    private static class NameComparator implements Comparator<TodoDocument> {

        @Override
        public int compare(TodoDocument todo1, TodoDocument todo2) {
            return todo1.getName().compareTo(todo2.getName());
        }
    }

    private static class DateComparator implements Comparator<TodoDocument> {

        @Override
        public int compare(TodoDocument todo1, TodoDocument todo2) {
            return todo2.getCreateDate().compareTo(todo1.getCreateDate());
        }
    }
}
