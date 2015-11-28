//from: https://github.com/karpathy/convnetjs/blob/master/demo/js/rldemo.js


// A single agent
    var DQNAgent = function(num_inputs, actions) {

      // positional information
      //this.p = new Vec(50, 50);
      //this.op = this.p; // old position
      //this.angle = 0; // direction facing


      this.actions = actions; /* array */
      /*
      this.actions.push([1,1]);
      this.actions.push([0.8,1]);
      this.actions.push([1,0.8]);
      this.actions.push([0.5,0]);
      this.actions.push([0,0.5]);
      */




      var num_actions = actions.length;
      var temporal_window = 1; // amount of temporal memory. 0 = agent lives in-the-moment :)
      var network_size = num_inputs*temporal_window + num_actions*temporal_window + num_inputs;

      // the value function network computes a value of taking any of the possible actions
      // given an input state. Here we specify one explicitly the hard way
      // but user could also equivalently instead use opt.hidden_layer_sizes = [20,20]
      // to just insert simple relu hidden layers.
      var layer_defs = [];
      layer_defs.push({type:'input', out_sx:1, out_sy:1, out_depth:network_size});
      layer_defs.push({type:'fc', num_neurons: 50, activation:'relu'});
      layer_defs.push({type:'fc', num_neurons: 50, activation:'relu'});
      layer_defs.push({type:'regression', num_neurons:num_actions});

      // options for the Temporal Difference learner that trains the above net
      // by backpropping the temporal difference learning rule.
      var tdtrainer_options = {learning_rate:0.001, momentum:0.0, batch_size:64, l2_decay:0.01};

      var opt = {};
      opt.temporal_window = temporal_window;
      opt.experience_size = 30000;
      opt.start_learn_threshold = 1000;
      opt.gamma = 0.7;
      opt.learning_steps_total = 200000;
      opt.learning_steps_burnin = 3000;
      opt.epsilon_min = 0.05;
      opt.epsilon_test_time = 0.05;
      opt.layer_defs = layer_defs;
      opt.tdtrainer_options = tdtrainer_options;

      this.brain = new deepqlearn.Brain(num_inputs, num_actions, opt);

    }

    Agent.prototype = {
        forward: function(input_array) {
            var actionix = this.brain.forward(input_array);
            var action = this.actions[actionix];
            this.actionix = actionix; //back this up
            return action;
        },
        backward: function(reward) {
            this.brain.backward(reward);
        }
    }


/*
    function draw_net() {
      if(simspeed <=1) {
        // we will always draw at these speeds
      } else {
        if(w.clock % 50 !== 0) return;  // do this sparingly
      }

      var canvas = document.getElementById("net_canvas");
      var ctx = canvas.getContext("2d");
      var W = canvas.width;
      var H = canvas.height;
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      var L = w.agents[0].brain.value_net.layers;
      var dx = (W - 50)/L.length;
      var x = 10;
      var y = 40;
      ctx.font="12px Verdana";
      ctx.fillStyle = "rgb(0,0,0)";
      ctx.fillText("Value Function Approximating Neural Network:", 10, 14);
      for(var k=0;k<L.length;k++) {
        if(typeof(L[k].out_act)==='undefined') continue; // maybe not yet ready
        var kw = L[k].out_act.w;
        var n = kw.length;
        var dy = (H-50)/n;
        ctx.fillStyle = "rgb(0,0,0)";
        ctx.fillText(L[k].layer_type + "(" + n + ")", x, 35);
        for(var q=0;q<n;q++) {
          var v = Math.floor(kw[q]*100);
          if(v >= 0) ctx.fillStyle = "rgb(0,0," + v + ")";
          if(v < 0) ctx.fillStyle = "rgb(" + (-v) + ",0,0)";
          ctx.fillRect(x,y,10,10);
          y += 12;
          if(y>H-25) { y = 40; x += 12};
        }
        x += 50;
        y = 40;
      }
    }
*/
/*
    var reward_graph = new cnnvis.Graph();
    function draw_stats() {
      var canvas = document.getElementById("vis_canvas");
      var ctx = canvas.getContext("2d");
      var W = canvas.width;
      var H = canvas.height;
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      var a = w.agents[0];
      var b = a.brain;
      var netin = b.last_input_array;
      ctx.strokeStyle = "rgb(0,0,0)";
      //ctx.font="12px Verdana";
      //ctx.fillText("Current state:",10,10);
      ctx.lineWidth = 10;
      ctx.beginPath();
      for(var k=0,n=netin.length;k<n;k++) {
        ctx.moveTo(10+k*12, 120);
        ctx.lineTo(10+k*12, 120 - netin[k] * 100);
      }
      ctx.stroke();

      if(w.clock % 200 === 0) {
        reward_graph.add(w.clock/200, b.average_reward_window.get_average());
        var gcanvas = document.getElementById("graph_canvas");
        reward_graph.drawSelf(gcanvas);
      }
    }
*/