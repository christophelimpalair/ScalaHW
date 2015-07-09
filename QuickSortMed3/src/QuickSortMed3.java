/**
 * Created by christophe on 4/7/15.
 */
public class QuickSortMed3 {

//    private void QuickSortMed(char A[], int Left, int Right)
//    {
//        int i, j, Mid, Median;
//        char v, temp;
//
//        System.out.println("Left: " + Left + " Right: " + Right);
//
//        if ( Left >= Right )
//            return;
//
//        Mid = (Left + Right) /2 ;
//
//        if ( A[Left] <= A[Mid] && A[Mid] <= A[Right] )
//            Median = Mid;
//        else if ( A[Left] >= A[Mid] && A[Mid] >= A[Right])
//            Median = Mid;
//        else if ( A[Mid] <= A[Left] && A[Left] <= A[Right])
//            Median = Left;
//        else if (A[Mid] >= A[Left] && A[Left] >= A[Right] )
//            Median = Left;
//        else
//            Median = Right;
//
//        temp = A[Median];
//        A[Median] = A[Right];
//        A[Right] = temp;
//
//        v = A[Right];
//        i = Left;
//        j = Right -1;
//
//        while ( true )
//        {
//            while (A[i] < v)
//                i++;
//
//            while ( j > Left && A[j] > v)
//                j--;
//
//            if (i >= j)
//                break;
//
//            temp = A[i];
//            A[i] = A[j];
//            A[j] = temp;
//            i++;
//            j--;
//        }
//
//        temp = A[i];
//        A[i] = A[Right];
//        A[Right] = temp;
//
//        if (Left < i-1)
//            QuickSortMed(A, Left, i - 1);
//
//        if (Right > i+1)
//            QuickSortMed(A, i + 1, Right);
//    }
//    private void QuickSort(char A[], int Left, int Right)
//    {
//        if (Left >= Right)
//            return;
//
//        v = A[Right];
//        i = Left;
//        j = Right - 1;
//
//        while ( true )
//        {
//            while(A[i] <v )
//                i++;
//
//            while (j > Left && A[j] > v )
//                j--;
//
//            if ( i >= j )
//                break;
//
//
//        }
//    }
//    public static void main(String []args)
//    {
//        QuickSortMed3 qs = new QuickSortMed3();
//        char[] A = {'T','H','E','Q','U','I','C','K','B','R','O','W','N',
//                'F','O','X','J','U','M','P','E','D','O','V','E','R','T','H','E','L','A','Z','Y','D','O','G','S'};
//        qs.QuickSortMed(A, 0, 36);
//    }
    private static char a[];
    public static void main(String[] args) {

        // Get a random generated array
        a = getArray();

        // prints the given array
//        printArray();

        // sort the array
        sort();

        System.out.println("");

        //prints the sorted array
//        printArray();

    }

    // This method sorts an array and internally calls quickSort
    public static void sort(){
        int left = 0;
        int right = a.length-1;

        quickSort(left, right);
    }

    // This method is used to sort the array using quicksort algorithm.
    // It takes the left and the right end of the array as the two cursors.
    private static void quickSort(int left,int right){
        // If both cursor scanned the complete array quicksort exits
        if(left >= right)
            return;
        System.out.println("Left: " + left + " Right: " + right);

        // For the simplicity, we took the right most item of the array as a pivot
        int pivot = a[right];
        int partition = partition(left, right, pivot);

        // Recursively, calls the quicksort with the different left and right parameters of the sub-array
        quickSort(0, partition-1);
        quickSort(partition+1, right);
    }

    // This method is used to partition the given array and returns the integer which points to the sorted pivot index
    private static int partition(int left,int right,int pivot){
        int leftCursor = left-1;
        int rightCursor = right;
        while(leftCursor < rightCursor){
            while(a[++leftCursor] < pivot);
            while(rightCursor > 0 && a[--rightCursor] > pivot);
            if(leftCursor >= rightCursor){
                break;
            }else{
                swap(leftCursor, rightCursor);
            }
        }
        swap(leftCursor, right);
        return leftCursor;
    }

    // This method is used to swap the values between the two given index
    public static void swap(int left,int right){
        char temp = a[left];
        a[left] = a[right];
        a[right] = temp;
    }

    public static void printArray(){
        for(int i : a){
            System.out.print(i+" ");
        }
    }

    public static char[] getArray(){
        char array[] = {'T','H','E','Q','U','I','C','K','B','R','O','W','N',
                'F','O','X','J','U','M','P','E','D','O','V','E','R','T','H','E','L','A','Z','Y','D','O','G','S'};
        return array;
    }

}
