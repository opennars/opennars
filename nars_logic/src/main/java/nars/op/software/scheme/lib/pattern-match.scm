#lang racket
(define (matches? pattern target)
  (cond ((symbol? pattern)
         true)
        ((and (list? pattern) (list? target))
         (matches-sequence? pattern target))
        (else false)))

(define (matches-sequence? pattern target)
  (cond ((matches? (car pattern) (car target))
         (matches-sequence? (cdr pattern) (cdr target)))
        (else false)))
         
(define (get-bindings pattern target)
  (cond ((symbol? pattern)
         (list (list pattern target)))
        ((list? pattern)
         (get-bindings-sequence pattern target))))

(define (get-bindings-sequence pattern target)
  (cond ((null? pattern)
         '())
        ((and (not (null? (cdr pattern))) 
              (eq? (cadr pattern) '...))
         (if (< (length (cddr pattern)) (length target))
             (concat (get-bindings (car pattern) (car target))
                   (get-bindings-sequence pattern (cdr target)))
             (get-bindings-sequence (cddr pattern) target)))
        (else
         (concat (get-bindings (car pattern) (car target)) 
               (get-bindings-sequence (cdr pattern) (cdr target))))))

(define (expand-ellipsis pattern target)
  (cond ((null? pattern)
         '())
        ((not (list? pattern))
         pattern)
        ((and (not (null? (cdr pattern))) 
              (eq? (cadr pattern) '...))
         (if (< (length (cddr pattern)) (length target))
             (cons (expand-ellipsis (car pattern) (car target))
                   (expand-ellipsis pattern (cdr target)))
             (expand-ellipsis (cddr pattern) target)))
        (else
         (cons (expand-ellipsis (car pattern) (car target)) 
               (expand-ellipsis (cdr pattern) (cdr target)))))) 

(define (expand template bindings)
  (cond ((null? template)
         '())
        ((symbol? (car template))
         (let ((binding (find-first (lambda (e) (eq? (car e) (car template))) bindings)))
           (if (null? binding)
               (cons (car template) (expand (cdr template) bindings))
               (cons (cadr binding) (expand (cdr template) (remove-first (lambda (e) (eq? (car e) (car tempate)))))))))
        ((list? template)
         (concat (expand-list template bindings) (expand ()
         (if (null? 
    

(define (reverse lst acc)
  (cond ((null? lst)
         acc)
        (else
         (reverse (cdr lst) (cons (car lst) acc)))))
 

(define (concat l1 l2)
  (if (null? l1) l2 
      (cons (car l1) (concat (cdr l1) l2))))

(define (find-first pred lst)
  (cond ((null? lst)
         '())
        ((pred (car lst))
         (car lst))
        (else
         (find-first pred (cdr lst)))))

(define (remove-first pred lst)
  (cond ((null? lst)
         '())
        ((pred (car lst))
         (cdr lst))
        (else 
         (cons (car lst) (remove-first pred (cdr lst))))))
      
(find-first (lambda (e) (eq? e 2)) '(1 2 3))

;(reverse '(1 2 3 4 5) '())
;(expand-ellipsis '(a (c d)... b) '(1 (5 6) (7 8) 3))
        

(get-bindings '(let ((a b) ...) body) '(let ((a 1) (b 2) (c 3)) 1))
        