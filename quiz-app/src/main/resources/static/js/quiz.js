
//
var _render = function(component, id = 'container') {
    ReactDOM.render(component, document.getElementById(id));
}


//
var _fetch = function(url, data, callback, self) {
    $.ajax({
        url: url,
        type: (data) ? 'post' : 'get',
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        data: JSON.stringify(data),
        success: function(data) {
            callback(data, self);
        },
        error: function(err, errorHandler, jqXHR) {
            console.error(url, err);
            var response = JSON.parse(err.responseText);
            $('#error-text').html('&nbsp' + response.message);
            $('#error-box').addClass('in');
            setTimeout(function() {
                console.log('fade out');
                $('#error-box').removeClass('in');
                location.reload();
            }, 2000);
        }
    });
}

// posts data, callback is called on successful data elements are url and body
var post = function(data, callback) {
    _fetch(data.url, data.body, callback, data.self);
 }

// get data from url
var get = function(url, callback, self) {
    _fetch(url, null, callback, self);
}

//
var LoginForm = React.createClass({
    getInitialState : function() {
        console.log("LoginForm getInitialState");
        var state = JSON.parse(localStorage.getItem('quiz-user') || '{}');
        state.new = (state.email === undefined);
        state.valid = !state.new;

        return state;
    },

    componentDidMount : function() {
        console.log("LoginForm componentDidMount");
    },

    login: function(email) {
        var self = this;
        // A callback to the form submit handler, so you can
        // keep all the state changes in the component.
        this.props.onFormSubmit({
            body: { email: email, quiz: 'CADEC2016' },
            url: "/api/login"
        }, function(data) {
            var state = { email: data.user.email };
            localStorage.setItem('quiz-user', JSON.stringify(state));
            self.setState(state);
            _render(<Quiz data={data} />);
        });
    },

    handleSubmit: function(e) {
        e.preventDefault();
        if (this.state.valid) {
            this.login(this.state.email);
        }
    },

    changeEmail: function(e) {
      var email = e.target.value.trim();
      this.setState({
            email: email,
            valid: (email && /\S+@\S+\.\S+/.test(email))
      });
    },

    render : function() {
        return (
                <div className="container-fluid text-left">
                    <div className="row content">
                        <div className="col-xs-12">
                            <h5 className="white">{(this.state.new) ? "Please register with your email address:": "Welcome back, press OK to continue and be prepared to answer the next question:"}</h5>
                        </div>
                    </div>
                    <div className="row content">
                        <div className="col-xs-10">
                            <input type="email" style={{width: '100%'}} autofocus="autofocus" name="email" value={this.state.email} onChange={this.changeEmail} disabled={!this.state.new} /><br/>
                        </div>
                        <div className="col-xs-2">
                            <a className="white" role="button" disabled={!this.state.valid} onClick={this.handleSubmit}>
                                <span className="glyphicon glyphicon-chevron-right" aria-hidden="true">OK</span>
                            </a>
                        </div>
                    </div>
                </div>
            );
    }
});

// Login class
var Login = React.createClass({
    onLogin: function(data, callback) {
       post(data, callback);
    },

    render: function() {
        console.log("Login render");
        return <LoginForm onFormSubmit={ this.onLogin } />
    }
});

//
var Continue = React.createClass({

    getInitialState: function() {
        return { text: this.props.text  };
    },

    render: function() {
        return (
                <div>
                        <h2>{ this.state.text }</h2>
                        <div>
                                <div>
                                        <button id="opt0" onClick={ this.props.a }>Nästa</button>
                                </div>
                        </div>
                </div>
        );
    }

});

//
var Button = React.createClass({
    render: function() {
        var classes = classNames('btn', 'btn-primary', 'btn-sm', 'btn-block');
        return (
            <div className="col-xs-6" style={{padding:0,margin:0}}>
                <button className={classes} id={this.props.id} onClick={this.props.a} disabled={this.props.disabled}>{this.props.text}</button>
            </div>
        );
    }
});

// alternatives
var Question = React.createClass({

    getInitialState: function() {
        return { q: this.props.q, disabled: false };
    },

    onClick : function(e) {
        var self = this;
        if (e.target.id != -1) {
            $(e.target).twinkle({ 'effectOptions': { 'color': 'rgba(67,68,145,0.5)', 'radius': 50 } });
        }
        this.props.a(e, function(score) {
            self.setState({disabled: true});
        });
    },

    disabled: function() {
        return this.state.disabled;
    },

    render: function() {
        return (
                <div className="container-fluid">
                    <div className="row">
                        <Button id="0" text={this.state.q.alternative1} a={this.onClick} disabled={this.disabled()} />
                        <Button id="1" text={this.state.q.alternative2} a={this.onClick} disabled={this.disabled()} />
                    </div>
                    <div className="row">
                        <Button id="2" text={this.state.q.alternative3} a={this.onClick} disabled={this.disabled()} />
                        <Button id="3" text={this.state.q.alternative4} a={this.onClick} disabled={this.disabled()} />
                    </div>
                </div>
        );
    }

});

// quizQuestion dialog
var Quiz = React.createClass({

    getInitialState: function() {
        console.log("Quiz getInitialState");
        var next = this.props.data.user.next;
        var length = this.props.data.quiz.quizQuestions.length;
        var current = -1;
        var progress = false;

        if (next == -1 || this.props.data.closed) {
            next = length;
        }

        if (next > 0) {
            current = next;
            next = Math.min(length, next+1);
            progress = (current < length);
        }

        var scores = new Array(length);
        this.props.data.user.scores.map(function(value, i) {
            scores[i] = value;
        });

        var state = { current: current, next: next, progress: progress,
            score: scores,
            countdown: this.props.data.quiz.thinkTimeInSeconds,
            result: { userResults: [] },
            fetching: false
            };

        console.log(state);

        return state;
    },

    startProgress: function() {
        var self = this;
        setTimeout(function() {
            if (self.state.progress) {
                self.setState({countdown: self.state.countdown - 1});
                if (self.state.countdown > 0) {
                    self.startProgress();
                } else {
                    console.log('Timeout....');
                    var e = {
                                preventDefault: function() {},
                                target: {
                                        id: -1
                                    }
                             };
                    self.setState({progress: false, countdown: self.props.data.quiz.thinkTimeInSeconds});
                    self.refs[self.state.current].onClick(e);
                }
            }
        }, 1000);
    },

    componentDidMount: function() {
        var self = this;
        $('#myCarousel').bind('slide.bs.carousel', function (e) {
            var current = self.state.next;
            var progress = (current < self.props.data.quiz.quizQuestions.length);
            var next = Math.min(current+1, self.props.data.quiz.quizQuestions.length);
            self.setState({ current: current, next: next, progress: progress, start: Date.now(), countdown: self.props.data.quiz.thinkTimeInSeconds});
            if (progress) {
                self.startProgress();
            } else {
                self.getResults();
            }
        });
        $('#myCarousel').carousel();
        console.log("componentDidMount", this.state);
        if (this.state.progress && self.state.next <= self.props.data.quiz.quizQuestions.length) {
            self.startProgress();
        } else if (self.state.next == self.props.data.quiz.quizQuestions.length) {
            this.getResults();
        }

    },

    answer: function(e, success) {
       e.preventDefault();
       console.log(e, this.state);
       post({
        url: '/api/answers/',
        body: {
            userId: this.props.data.user.id,
            quizQuestionId: this.props.data.quiz.quizQuestions[this.state.current].quizQuestionId,
            actualAnswer: +e.target.id,
            elapsed: (Date.now() - this.state.start)
        },
        self: this
       }, function(data, self) {
            console.log("Answer successfully posted: " + data.status);
            var score = self.state.score;
            score[self.state.current] = data.status;
            self.setState({progress: false, score: score});
            if (success) {
                success(data.status);
            }
       });

    },

    onLogin: function(data, callback) {
       post(data, callback);
    },

    getResults: function() {
            var self = this;
            self.setState({ fetching: true });
            get(
                '/api/quizzes/' + this.props.data.quiz.name + '/results',
                function(data, self) {
                    self.setState({ result: data, fetching: false });
                },
                self
            );
    },

    render: function() {
        console.log("Quiz render");

        var self = this;
        var quiz = this.props.data.quiz;
        var lastItem = (this.state.current == this.props.data.quiz.quizQuestions.length);

        console.log('lastItem: ' + lastItem);

        var nextClasses = classNames('right', 'carousel-control', { hidden: this.state.progress || lastItem});
        var countdownClasses = classNames('countdown', { hidden: !this.state.progress });
        var firstIndClasses = classNames({ active: this.state.next == 0 });
        var firstItemClasses = classNames('item', { active: this.state.next == 0 });
        var lastItemClasses = classNames('item', { active: lastItem });
        var refreshClasses = classNames('glyphicon', 'glyphicon-refresh', { 'text-muted': this.state.fetching});

        console.log(this.state);

        return (
        <div className="container-fluid">
            <div id="myCarousel" className="carousel slide" data-ride="carousel" data-interval="false" data-keyboard="false">
                <ol className="carousel-indicators">
                    <li className={firstIndClasses} data-target="#myCarousel"></li>
                    {quiz.quizQuestions.map(function(row, i) {
                        var classes = classNames({active: self.state.current == i });
                        return <li key={i} className={classes} data-target="#myCarousel"></li>;
                    })}
                    <li data-target="#myCarousel"></li>
                </ol>

                <div className="carousel-inner" role="listbox">
                    <div className={firstItemClasses}>
                        <div className="carousel-content">
                            <h3>
                                Welcome!
                            </h3>
                            <p>
                                <strong>{this.props.data.user.email}</strong>
                            </p>
                            <p>
                                You have {quiz.thinkTimeInSeconds} seconds to answer each question, and the Quiz consists of {quiz.quizQuestions.length} questions.
                            </p>
                            <p>
                                The Quiz is open until the end of CADEC seminars, you'll be notified! <br/>
                                Click on the arrow to the right to start quizzing.
                            </p>
                        </div>
                    </div>

                    {quiz.quizQuestions.map(function(row, i) {
                        var classes = classNames('item', { active: self.state.current == i });
                        return (
                              <div key={i} className={classes}>
                                <div className="carousel-content">
                                    <h4>
                                        {row.question}?
                                    </h4>
                                    <Question ref={i} q={row} a={self.answer} />
                                </div>
                              </div>
                        );
                    })}

                    <div className={lastItemClasses}>
                        <div className="left">
                            {(this.state.result.closed === true) ? 'Final Scoreboard' : 'Current Standings'}
                        </div>
                        <a className="white right" role="button" onClick={this.getResults}><span className={refreshClasses}></span></a>
                        <div className="carousel-content">
                          {self.state.result.userResults.map(function(r, i) {
                                if (self.state.result.closed === true && r.user.email === self.props.data.user.email) {
                                console.log('return header row');
                                    return (
                                        <p>
                                            <strong>Place #{i+1} - {i == 0 ? "Congrats!" : "Thanks!"}</strong>
                                        </p>
                                    );
                                }
                            })
                          }
                            <table className="table table-condensed">
                                <thead>
                                  <tr>
                                    <th style={{'text-align': 'right'}}>#</th>
                                    <th>Email</th>
                                    <th style={{'text-align': 'right'}}>Score</th>
                                    <th style={{'text-align': 'right'}}>Time</th>
                                  </tr>
                                </thead>
                                <tbody>
                                {this.state.result.userResults.map(function(r, i) {
                                    return(
                                        <tr key={i}>
                                            <td style={{'text-align': 'right'}}>{i+1}</td>
                                            <td>{r.user.email}</td>
                                            <td style={{'text-align': 'right'}}>{self.state.result.closed ? r.score : '****'}</td>
                                            <td style={{'text-align': 'right'}}>{self.state.result.closed ? parseFloat(r.elapsed / 1000.0).toFixed(1) : '****'}</td>
                                        </tr>
                                        );
                                    })
                                }
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div className={countdownClasses}>
                        <h5>{self.state.countdown}</h5>
                    </div>

                </div>

                <a className={nextClasses} href="#myCarousel" role="button" data-slide="next">
                  <span className="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
                </a>
            </div>
        </div>
        );
    }
});

$(document).ready(function () {
    _render(<Login />);
});