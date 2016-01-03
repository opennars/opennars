(define (list . xs) xs)

(define pair? list?)

(define nil '())

(define (map f lst)
  (cond ((null? lst)
         lst)
        (else (cons (f (car lst)) (map f (cdr lst))))))

(define (length lst)
  (cond ((null? lst) 
         0)
        (else (+ 1 (length (cdr lst))))))

(define (newline) (display "\n"))

(define (compose f . fs)
    (lambda (x)
        (define (comp fs)
          (cond ((null? fs) 
                 x)
                (else 
                 ((car fs) (comp (cdr fs))))))
      (comp (cons f fs))))
                 

(define (cadr x) ((compose car cdr) x))
(define (cddr x) ((compose cdr cdr) x))
(define (cdddr x) ((compose cdr cddr) x))
(define (caddr x) ((compose car cddr) x))
(define (cadddr x) ((compose cadr cddr) x))
(define (caadr x) ((compose car car cdr) x))
(define (cdadr x) ((compose cdr cadr) x))

'STD-LIB-LOADED