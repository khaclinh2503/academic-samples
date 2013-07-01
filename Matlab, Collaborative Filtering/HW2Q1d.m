function [] = HW2Q1d
    AlexRowIndex = 500;
    LastColToCheck = 100;
    NumTopShows = 5;
    R = load('user-shows.txt');
    maxK = 19;
    
    Alex = load('alex.txt');
    shows = importdata('shows.txt');

    [m, n] = size(R);

    X = R*R';
    P = zeros(m,m);

    for i = 1:m
        P(i,i) = X(i,i);
    end

    SU = (P^-.5)*(X*(P^-.5));
    GammaU = SU*R;

    [sortedUValues, sortedUIndex] = sort(GammaU(AlexRowIndex,1:LastColToCheck),'descend');

    disp('User-User Collaborative Filtering:');
    for i = 1:NumTopShows
        disp(shows(sortedUIndex(i)));
        disp(sortedUValues(i));
    end

    Y = R'*R;
    Q = zeros(n,n);

    for i = 1:n
        Q(i,i) = Y(i,i);
    end

    SI = (Q^-.5)*(Y*(Q^-.5));
    GammaI = R*SI;

    [sortedIValues, sortedIIndex] = sort(GammaI(AlexRowIndex,1:LastColToCheck),'descend');

    disp('Item-Item Collaborative Filtering:');
    for i = 1:NumTopShows
        disp(shows(sortedIIndex(i)));
        disp(sortedIValues(i));
    end
    
    disp('User-User Collaborative Filtering:');
    for k = 1:maxK
        [precision] = PrecisionAtTopK(Alex, GammaU, k, AlexRowIndex, LastColToCheck);
        disp(['k = ', num2str(k), '; precision = ', num2str(precision)]);
    end
    
    disp('Item-Item Collaborative Filtering:');
    for k = 1:maxK
        [precision] = PrecisionAtTopK(Alex, GammaI, k, AlexRowIndex, LastColToCheck);
        disp(['k = ', num2str(k), '; precision = ', num2str(precision)]);
    end
end

function [precision] = PrecisionAtTopK(Alex, Gamma, k, AlexRowIndex, LastColToCheck)
    [sortedValues, sortedIndex] = sort(Gamma(AlexRowIndex,1:LastColToCheck),'descend');

    precision = 0.0;

    for i = 1:k
        if Alex(sortedIndex(i)) == 1;
            precision = precision + 1;
        end
    end

    precision = precision / k;
end

