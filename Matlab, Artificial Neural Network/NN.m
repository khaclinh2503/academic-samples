function [] = NN
    % A Simple Neural Network for performing digit recognition.
    % Philip Scuderi

    % Load Training Data
    fprintf('Loading Data ...\n')

    load('ex3data1.mat');
    m = size(X, 1); %#ok<NODEF>

    % Load the weights into variables Theta1 and Theta2
    load('ex3weights.mat');

    pred = predict(Theta1, Theta2, X);

    fprintf('\nTraining Set Accuracy: %f\n', mean(double(pred == y)) * 100);

    fprintf('Program paused. Press enter to continue.\n');
    pause;

    %  To give you an idea of the network's output, you can also run
    %  through the examples one at the a time to see what it is predicting.

    %  Randomly permute examples
    rp = randperm(m);

    for i = 1:m
        % Display 
        fprintf('\nDisplaying Example Image\n');
        displayData(X(rp(i), :));

        pred = predict(Theta1, Theta2, X(rp(i),:));
        fprintf('\nNeural Network Prediction: %d (digit %d)\n', pred, mod(pred, 10));

        % Pause
        fprintf('Program paused. Press enter to continue.\n');
        pause;
    end

end

function [h, display_array] = displayData(X, example_width)
    %DISPLAYDATA Display 2D data in a nice grid
    %   [h, display_array] = DISPLAYDATA(X, example_width) displays 2D data
    %   stored in X in a nice grid. It returns the figure handle h and the 
    %   displayed array if requested.

    % Set example_width automatically if not passed in
    if ~exist('example_width', 'var') || isempty(example_width) 
        example_width = round(sqrt(size(X, 2)));
    end

    % Gray Image
    colormap(gray);

    % Compute rows, cols
    [m n] = size(X);
    example_height = (n / example_width);

    % Compute number of items to display
    display_rows = floor(sqrt(m));
    display_cols = ceil(m / display_rows);

    % Between images padding
    pad = 1;

    % Setup blank display
    display_array = - ones(pad + display_rows * (example_height + pad), ...
                           pad + display_cols * (example_width + pad));

    % Copy each example into a patch on the display array
    curr_ex = 1;
    for j = 1:display_rows
        for i = 1:display_cols
            if curr_ex > m, 
                break; 
            end
            % Copy the patch

            % Get the max value of the patch
            max_val = max(abs(X(curr_ex, :)));
            display_array(pad + (j - 1) * (example_height + pad) + (1:example_height), ...
                          pad + (i - 1) * (example_width + pad) + (1:example_width)) = ...
                            reshape(X(curr_ex, :), example_height, example_width) / max_val;
            curr_ex = curr_ex + 1;
        end
        if curr_ex > m, 
            break; 
        end
    end

    % Display Image
    h = imagesc(display_array, [-1 1]);

    % Do not show axis
    axis image off

    drawnow;
end

function p = predict(Theta1, Theta2, X)
    %PREDICT Predict the label of an input given a trained neural network
    %   p = PREDICT(Theta1, Theta2, X) outputs the predicted label of X given the
    %   trained weights of a neural network (Theta1, Theta2)

    a1 = [ones(size(X,1),1) X];
    
    z2 = a1 * Theta1';
    a2 = [ones(size(z2,1),1) sigmoid(z2)];

    z3 = a2 * Theta2';
    a3 = sigmoid(z3);

    [dontCare, p] = max(a3, [], 2); %#ok<ASGLU>
end

function g = sigmoid(z)
    %SIGMOID Compute sigmoid functoon
    %   J = SIGMOID(z) computes the sigmoid of z.

    g = 1.0 ./ (1.0 + exp(-z));
end