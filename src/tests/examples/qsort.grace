fun main() : nothing
    var a: int[10]; var m,j,i,n: int;
    fun quicksort (ref a : int[]; m, n : int) : nothing
        var i, j, temp : int;
    {
    if n <= m then return;
    i <- m; j <- n;
    while i <= j do {
        while a[i] < a[(m+n) div 2] do i <- i+1;
        while a[j] > a[(m+n) div 2] do j <- j-1;
        if i <= j then {
            temp <- a[i]; a[i] <- a[j]; a[j] <- temp;
            i <- i+1; j <- j-1;
        }
    }
}
{
    quicksort(a, m, j); quicksort(a, i, n);
}
